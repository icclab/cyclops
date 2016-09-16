package ch.icclab.cyclops.timeseries;
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

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 02/09/16
 * Description: Helper class for DB point generation
 */
public class GenerateDBPoint {

    /**
     * Generate DB Point Builder from specified object
     * @param object to be processed
     * @return Point.Builder
     */
    public static Point.Builder fromObject(Object object) {
        return generate(object, null, null, null);
    }

    /**
     * Generate DB Point Builder from specified object
     * @param object to be processed
     * @param timeField name
     * @param unit of time
     * @return Point.Builder
     */
    public static Point.Builder fromObjectWithTime(Object object, String timeField, TimeUnit unit) {
        return generate(object, timeField, unit, null);
    }

    /**
     * Generate DB Point Builder from specified object
     * @param object to be processed
     * @param tags list
     * @return Point.Builder
     */
    public static Point.Builder fromObjectWithTags(Object object, List<String> tags) {
        return generate(object, null, null, tags);
    }

    /**
     * Generate DB Point Builder from specified object
     * @param object to be processed
     * @param timeField name
     * @param unit of time
     * @param tags list
     * @return Point.Builder
     */
    public static Point.Builder fromObjectWithTimeAndTags(Object object, String timeField, TimeUnit unit, List<String> tags) {
        return generate(object, timeField, unit, tags);
    }

    /**
     * Generate a DBPoint
     * @param object to be serialised
     * @param time field
     * @param unit of time
     * @param tags list of tags
     * @return Point.Builder or null
     */
    private static Point.Builder generate(Object object, String time, TimeUnit unit, List<String> tags) {
        try {
            // flatten possibly nested object
            Map<String, Object> flat = JsonFlattener.flattenAsMap(new Gson().toJson(object));

            // create point builder
            Point.Builder builder = Point.measurement(object.getClass().getSimpleName());

            // if time field was provided
            if (time != null && unit != null && flat.containsKey(time)) {
                builder.time(TimeStamp.cast(flat.get(time)), unit);
                flat.remove(time);
            } else {
                builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }

            // if list of tags was provided
            if (tags != null && !tags.isEmpty()) {
                for (String tag: tags) {
                    if (flat.containsKey(tag) && flat.get(tag) instanceof String) {
                        builder.tag(tag, (String) flat.get(tag));
                        flat.remove(tag);
                    }
                }
            }

            // finally add individual fields
            flat.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> builder.field(entry.getKey(), entry.getValue()));

            return builder;
        } catch (Exception ignored) {
            return null;
        }
    }
}
