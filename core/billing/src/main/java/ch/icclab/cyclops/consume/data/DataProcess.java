package ch.icclab.cyclops.consume.data;
/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.*;
import ch.icclab.cyclops.util.RegexParser;
import ch.icclab.cyclops.util.loggers.DataLogger;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;
import org.influxdb.dto.Point;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 09/08/16
 * Description: Process data frame in runnable manner
 */
public class DataProcess implements Runnable {

    private String content;

    private static Messenger messenger = Messenger.getInstance();

    private PublisherCredentials publisherSettings;
    private String defaultMeasurementName;

    private List<Object> broadcast;
    private BatchPointsContainer container;
    private Map<String, List<Object>> dispatch;

    public static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public DataProcess(String content, PublisherCredentials publisher, String measurement) {
        this.publisherSettings = publisher;
        this.defaultMeasurementName = measurement;
        this.container = new BatchPointsContainer();
        this.broadcast = new ArrayList<>();
        this.dispatch = new HashMap<>();
        this.content = content;
    }

    @Override
    public void run() {
        try {
            // try to map it as array
            List<Map> array = new Gson().fromJson(content, List.class);

            // process individual items (cannot be in parallel as we would quickly start thread-context switching)
            array.stream().forEach(this::processDataFrame);

        } catch (Exception ignored) {
            // this means it was not an array to begin with, just a simple object
            processDataFrame(content);
        }

        finalise();
    }

    private void finalise() {

        // schedule tasks
        TaskExecutor executor = TaskExecutor.getInstance();

        // persist points
        if (container.size() == 1) {
            // when only one point in container use shared InfluxDB session for delaying writes
            executor.addTask(() -> SharedInfluxDBSession.getSession().persistSinglePoint(container.getFirstPoint()));
        } else if (container.size() > 1) {
            // we did batch persisting manually so we can store it immediately
            executor.addTask(() -> {new InfluxDBClient().persistContainer(container);});
        }

        // broadcast
        if (broadcast != null && !broadcast.isEmpty()) {
            if (broadcast.size() == 1) {
                // broadcast it as value
                executor.addTask(() -> {messenger.broadcast(broadcast.get(0));});
            } else {
                // broadcast it as array
                executor.addTask(() -> {messenger.broadcast(broadcast);});
            }
        }

        // dispatch
        if (dispatch != null && !dispatch.isEmpty()) {
            for (Map.Entry entry: dispatch.entrySet()) {
                List<Object> list = (List<Object>) entry.getValue();

                if (list.size() == 1) {
                    // publish it as value
                    executor.addTask(() -> {messenger.publish(list.get(0), (String) entry.getKey());});
                } else {
                    // publish it as array
                    executor.addTask(() -> {messenger.publish(list, (String) entry.getKey());});
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

                // extract tags from flattened json
                Map<String, String> tags = extractTags(flat, guideline.getTagNames());

                // parse time field if it was provided
                String timeField = guideline.getTimeField();
                Long timeStamp = (timeField != null && flat.containsKey(timeField))? TimeStamp.cast(flat.get(timeField)): null;
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
        return Point.measurement(measurementName).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).fields(RemoveNullValues.fromMap(fields));
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
        } else {
            builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
}
