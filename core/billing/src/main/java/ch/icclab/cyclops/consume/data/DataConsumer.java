package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.BatchPointsContainer;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.RemoveNullValues;
import ch.icclab.cyclops.util.RegexParser;
import ch.icclab.cyclops.util.loggers.DataLogger;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    final static Logger logger = LogManager.getLogger(DataConsumer.class.getName());

    private static InfluxDBClient influxDBClient = InfluxDBClient.getInstance();
    private static Messenger messenger = Messenger.getInstance();

    public static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private PublisherCredentials publisherSettings;
    private String defaultMeasurementName;

    private Long validRecords;

    private BatchPointsContainer container;
    private List<Object> broadcast;
    private Map<String, List<Object>> dispatch;

    public DataConsumer(String name, PublisherCredentials settings) {
        this.defaultMeasurementName = name;
        this.publisherSettings = settings;
    }

    @Override
    public void consume(String content) {
        initialise();

        try {
            // try to map it as array
            List<Map> array = new Gson().fromJson(content, List.class);

            // iterate over object entries
            array.forEach(this::processDataFrame);

        } catch (Exception ignored) {
            // this means it was not an array to begin with, just a simple object
            processDataFrame(content);
        }

        finalise();
    }

    public Long getNumberOfValidRecords() {
        return validRecords;
    }

    private void initialise() {
        validRecords = 0l;
        container = new BatchPointsContainer();
        broadcast = new ArrayList<>();
        dispatch = new HashMap<>();
    }

    private void finalise() {
        // persist points that were created
        influxDBClient.persistContainer(container);

        // broadcast
        if (broadcast != null && !broadcast.isEmpty()) {
            if (broadcast.size() == 1) {
                // broadcast it as value
                messenger.broadcast(broadcast.get(0));
            } else {
                // broadcast it as array
                messenger.broadcast(broadcast);
            }
        }

        // dispatch
        if (dispatch != null && !dispatch.isEmpty()) {
            for (Map.Entry entry: dispatch.entrySet()) {
                List<Object> list = (List<Object>) entry.getValue();

                if (list.size() == 1) {
                    // publish it as value
                    messenger.publish(list.get(0), (String) entry.getKey());
                } else {
                    // publish it as array
                    messenger.publish(list, (String) entry.getKey());
                }
            }
        }
    }

    /**
     * Process data frame as OBJECT
     * @param obj to be processed
     */
    private void processDataFrame(Map obj) {
        storeAndPublish(obj);
    }

    /**
     * Process data frame as STRING
     * @param str to be processed
     */
    private void processDataFrame(String str) {
        try {
            Map obj = new Gson().fromJson(str, Map.class);

            storeAndPublish(obj);
        } catch (Exception ignored) {
            // if incoming JSON is not valid we are simply skipping it
        }
    }

    /**
     * Process, store and publish incoming object
     * @param obj representation
     */
    private void storeAndPublish(Map obj) {
        try {
            // now parse it and create a database Point
            ConsumedData data = processDataAndGetPoint(obj);

            // and finally persist it
            container.addPoint(data.getPoint());

            // publish or broadcast if desired
            if (data.shouldPublish()) {

                // reapply class definition
                obj.put(DataMapping.FIELD_FOR_MAPPING, data.getClazz());

                // where to publish
                if (data.doNotBroadcastButRoute()) {
                    String clazz = data.getClazz();

                    // add to internal dispatch structure
                    if (dispatch.containsKey(clazz)) {
                        // we need to fetch the list
                        List<Object> list = dispatch.get(clazz);
                        list.add(obj);
                    } else {
                        // we need to create a new one
                        List<Object> list = new ArrayList<>();
                        list.add(obj);

                        dispatch.put(clazz, list);
                    }

                } else {
                    broadcast.add(obj);
                }
            }

            validRecords += 1;

        } catch (Exception notValidJson) {
            DataLogger.log(String.format("Received event/measurement is not a valid JSON: %s", notValidJson.getMessage()));
        }
    }

    /**
     * Data holder for consumed data including database point and mapping guidelines
     */
    private class ConsumedData {
        private String clazz;
        private Point.Builder builder;
        private Boolean shouldPublish;
        private Boolean doNotBroadcastButRoute;

        public ConsumedData(String clazz, Point.Builder builder, Boolean shouldPublish, Boolean doNotBroadcastButRoute) {
            this.clazz = clazz;
            this.builder = builder;
            this.shouldPublish = shouldPublish;
            this.doNotBroadcastButRoute = doNotBroadcastButRoute;
        }

        public ConsumedData(String clazz, Point.Builder builder, DataMapping guideline) {
            this.clazz = clazz;
            this.builder = builder;
            this.shouldPublish = guideline.shouldPublish();
            this.doNotBroadcastButRoute = guideline.doNotBroadcastButRoute();
        }

        public String getClazz() {
            return clazz;
        }
        public Point.Builder getPoint() {
            return builder;
        }
        public Boolean shouldPublish() {
            return shouldPublish;
        }
        public Boolean doNotBroadcastButRoute() {
            return doNotBroadcastButRoute;
        }
    }

    /**
     * Process and create database Point
     * @param map to be processed
     * @return point
     */
    private ConsumedData processDataAndGetPoint(Map<String, Object> map) {
        try {
            // check whether type field is present
            String clazz = (String) map.get(DataMapping.FIELD_FOR_MAPPING);

            // find corresponding classes
            Set set = new ClassesInPackageScanner().setResourceNameFilter((packageName, fileName)
                    -> clazz.equals(RegexParser.getFileName(fileName))).scan(DataConsumer.class.getPackage().getName());

            // move to the first element
            Optional<Class> first = set.stream().findFirst();

            if (first.isPresent()) {
                // access object based on provided class definition
                DataMapping guideline = (DataMapping) Class.forName(first.get().getName()).newInstance();

                DataLogger.log(String.format("Event/measurement received with \"%s\" guidelines", clazz));

                // by default we will work with the original map
                Map<String, Object> processed = map;
                try {
                    // let's call user specified pre-processing method
                    Map<String, Object> preProcessing = guideline.preProcess(map);

                    // assign new one if it's valid
                    if (preProcessing != null && !preProcessing.isEmpty()) {
                        processed = preProcessing;
                    }
                } catch (Exception ignored) {
                }

                // we will be storing flattened version of the map
                Map<String, Object> flat = JsonFlattener.flattenAsMap(new Gson().toJson(processed));

                // remove type from original HashMap
                flat.remove(DataMapping.FIELD_FOR_MAPPING);

                // extract tags from flattened json
                Map<String, String> tags = extractTags(flat, guideline.getTagNames());

                // parse time field if it was provided
                String timeField = guideline.getTimeField();
                Long timeStamp = (timeField != null && flat.containsKey(timeField))? getTimeStamp(flat.get(timeField)): null;
                if (timeStamp != null && timeStamp >= 0) flat.remove(timeField);

                // determine correct time unit
                TimeUnit unit = guideline.getTimeUnit();
                if (unit == null) {
                    unit = TIME_UNIT;
                }
                // finally construct database point
                Point.Builder builder = constructPoint(clazz, timeStamp, unit, flat, tags);
                return new ConsumedData(clazz, builder, guideline);
            } else {
                // guideline was not present or wasn't valid
                throw new Exception();
            }

        } catch (Exception withoutGuidelines){
            // mapping failed, guideline was not provided, use default
            DataLogger.log("Event/measurement received without valid guidelines");

            Point.Builder builder;

            Map<String, Object> flat = JsonFlattener.flattenAsMap(new Gson().toJson(map));

            // even though we don't have mapping template, we still want to have proper measurement name if TYPE is present
            if (flat.containsKey(DataMapping.FIELD_FOR_MAPPING)){

                // get that name
                String measurement = (String) flat.get(DataMapping.FIELD_FOR_MAPPING);

                // remove it from flat
                flat.remove(DataMapping.FIELD_FOR_MAPPING);

                // construct point
                builder = constructPoint(measurement, flat);
            } else {
                builder = constructPoint(defaultMeasurementName, flat);
            }

            // determine publisher preferences
            Boolean shouldPublish = publisherSettings.getPublisherIncludeAlsoUnknown();
            Boolean doNotRouteButBroadcast = publisherSettings.getPublisherByDefaultDispatchInsteadOfBroadcast();
            String clazz = Loader.getSettings().getInfluxDBCredentials().getInfluxDBDefaultMeasurement();

            return new ConsumedData(clazz, builder, shouldPublish, doNotRouteButBroadcast);
        }
    }

    /**
     * Construct database Point
     * @param measurementName to be used
     * @param fields as map
     * @return point builder
     */
    private Point.Builder constructPoint(String measurementName, Map<String, Object> fields) {
        return Point.measurement(measurementName).fields(RemoveNullValues.fromMap(fields));

    }

    /**
     * Construct database Point
     * @param measurementName to be used
     * @param timeStamp to be marked
     * @param unit of time to be used
     * @param fields as map
     * @param tags as map
     * @return point builder
     */
    private Point.Builder constructPoint(String measurementName, Long timeStamp, TimeUnit unit, Map<String, Object> fields, Map<String, String> tags) {
        Point.Builder builder = Point.measurement(measurementName);

        // apply time stamp with time unit
        if (timeStamp != null && timeStamp >= 0 && unit != null) {
            builder.time(timeStamp, unit);
        }

        // apply specified tags
        if (tags != null && !tags.isEmpty()) {
            builder.tag(RemoveNullValues.fromMap(tags));
        }

        // and finally add fields
        builder.fields(RemoveNullValues.fromMap(fields));

        return builder;
    }

    /**
     * Move tags from content into variable based on provided list of tags
     * @param content to be extracted
     * @param tagList definition
     * @return extracted tags or null
     */
    private Map<String, String> extractTags(Map<String, Object> content, List<String> tagList) {
        if (tagList != null) {
            Map<String, String> tags = new HashMap<>();

            // iterate over list of tags and move them from the original content
            for (String tag: tagList) {
                tags.put(tag, (String) content.get(tag));
                content.remove(tag);
            }

            return tags;
        } else {
            return null;
        }
    }

    /**
     * Parse long from unknown type
     * @param unknown object
     * @return Long or null
     */
    private Long getTimeStamp(Object unknown) {
        try {
            BigDecimal dec = (BigDecimal) unknown;
            return dec.longValue();
        } catch (Exception a) {
            try {
                return Long.parseLong((String) unknown);
            } catch (Exception b) {
                try {
                    return (Long) unknown;
                } catch (Exception c) {
                    try {
                        return Long.valueOf(unknown.toString());
                    } catch (Exception d) {
                        return null;
                    }
                }
            }
        }
    }
}
