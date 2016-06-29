package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.Messenger;
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

    public static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private PublisherCredentials publisherSettings;
    private String defaultMeasurementName;

    public DataConsumer(String name, PublisherCredentials settings) {
        this.defaultMeasurementName = name;
        this.publisherSettings = settings;
    }

    @Override
    protected void consume(String content) {
        try {
            // try to map it as array
            List array = new Gson().fromJson(content, List.class);

            // iterate over object entries
            for (Object obj: array) {
                processDataFrame(obj);
            }

        } catch (Exception ignored) {
            // this means it was not an array to begin with, just a simple object
            processDataFrame(content);
        }
    }

    /**
     * Process data frame as OBJECT
     * @param obj to be processed
     */
    private void processDataFrame(Object obj) {
        String json = new Gson().toJson(obj);

        storeAndPublish(json, obj);
    }

    /**
     * Process data frame as STRING
     * @param str to be processed
     */
    private void processDataFrame(String str) {
        try {
            Object obj = new Gson().fromJson(str, Object.class);

            storeAndPublish(str, obj);
        } catch (Exception ignored) {
        }
    }

    /**
     * Process JSON object
     * @param json as JSON string
     */
    private void storeAndPublish(String json, Object obj) {
        try {
            // flatten JSON and map it to HashMap
            Map<String, Object> flat = JsonFlattener.flattenAsMap(json);

            // now parse it and create a database Point
            ConsumedData data = processDataAndGetPoint(flat);

            // and finally persist it
            influxDBClient.persistSinglePoint(data.getPoint());

            // publish or broadcast if desired
            if (data.shouldPublish()) {
                Messenger messenger = Messenger.getInstance();

                // where to publish
                if (data.doNotBroadcastButRoute()) {
                    messenger.publish(obj, data.getRoutingKey());
                } else {
                    messenger.broadcast(obj);
                }
            }

        } catch (Exception notValidJson) {
            DataLogger.log(String.format("Received event/measurement is not a valid JSON: %s", notValidJson.getMessage()));
        }
    }

    /**
     * Data holder for consumed data including database point and mapping guidelines
     */
    private class ConsumedData {
        private Point.Builder builder;
        private Boolean shouldPublish;
        private Boolean doNotBroadcastButRoute;
        private String routingKey;

        public ConsumedData(Point.Builder builder, Boolean shouldPublish, Boolean doNotBroadcastButRoute, String routingKey) {
            this.builder = builder;
            this.routingKey = routingKey;
            this.shouldPublish = shouldPublish;
            this.doNotBroadcastButRoute = doNotBroadcastButRoute;
        }

        public ConsumedData(Point.Builder builder, DataMapping guideline, String routingKey) {
            this.builder = builder;
            this.routingKey = routingKey;
            this.shouldPublish = guideline.shouldPublish();
            this.doNotBroadcastButRoute = guideline.doNotBroadcastButRoute();
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

        public String getRoutingKey() {
            return routingKey;
        }
        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    /**
     * Process and create database Point
     * @param flat map
     * @return point
     */
    private ConsumedData processDataAndGetPoint(Map<String, Object> flat) {
        try {
            // check whether type field is present
            String clazz = (String) flat.get(DataMapping.FIELD_FOR_MAPPING);

            // find corresponding classes
            Set set = new ClassesInPackageScanner().setResourceNameFilter((packageName, fileName)
                    -> clazz.equals(RegexParser.getFileName(fileName))).scan(DataConsumer.class.getPackage().getName());

            // move to the first element
            Optional<Class> first = set.stream().findFirst();

            if (first.isPresent()) {
                // access object based on provided class definition
                DataMapping guideline = (DataMapping) Class.forName(first.get().getName()).newInstance();

                String measurement = (String) flat.get(DataMapping.FIELD_FOR_MAPPING);

                DataLogger.log(String.format("Event/measurement received with \"%s\" guidelines", measurement));

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
                Point.Builder builder = constructPoint(measurement, timeStamp, unit, flat, tags);
                return new ConsumedData(builder, guideline, clazz);
            } else {
                // guideline was not present or wasn't valid
                throw new Exception();
            }

        } catch (Exception withoutGuidelines){
            // mapping failed, guideline was not provided, use default
            DataLogger.log("Event/measurement received without valid guidelines");

            Point.Builder builder;

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
            String defaultRoutingKey = Loader.getSettings().getInfluxDBCredentials().getInfluxDBDefaultMeasurement();

            return new ConsumedData(builder, shouldPublish, doNotRouteButBroadcast, defaultRoutingKey);
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
