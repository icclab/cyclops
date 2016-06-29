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

import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.influxdb.dto.QueryResult;
import org.joda.time.DateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 17/05/16
 * Description: Parse InfluxDB's Query Result
 */
public class ParseQueryResult {

    private static String TIME_FIELD = "time";

    public static List<Map> parse(QueryResult data) {
        // structure of QueryResult
        List<String> columns = new ArrayList<>();

        // we need to record columns just once
        Boolean onlyOnce = true;

        // index of Time field
        int time = -1;

        // container for result
        List<Map> container = new ArrayList<>();

        // mapping
        Gson gson = new Gson();

        try {
            // go over all results
            for (QueryResult.Result result : data.getResults()) {
                // every series
                for (QueryResult.Series serie : result.getSeries()) {

                    // add all column names
                    if (onlyOnce) {
                        columns.addAll(serie.getColumns());
                        time = columns.indexOf(TIME_FIELD);
                        onlyOnce = false;
                    }
                    // iterate over values
                    for (List<Object> values : serie.getValues()) {
                        // we will store one row here
                        Map<String, Object> row = new HashMap<>();

                        // access objects
                        int i = 0;
                        for (Object value: values) {

                            // timestamp needs to be in Long and not String
                            if (time == i) {
                                try {
                                    // return seconds not milliseconds
                                    value = new DateTime(value).getMillis() / 1000;
                                } catch (Exception ignored) {
                                }
                            }

                            row.put(columns.get(i), value);
                            i++;
                        }

                        // delete meta field
                        row.remove(InfluxDBCredentials.COUNTER_FIELD_NAME);

                        // de-flatten structure
                        String rich = JsonUnflattener.unflatten(gson.toJson(row));
                        container.add(gson.fromJson(rich, Map.class));
                    }
                }
            }
        } catch (Exception ignored) {
            // in case of empty QueryResult body do nothing
        }

        return container;
    }

    public static List map(List<Map> list, Class clazz) {
        List<Object> mapped = new ArrayList<>();

        // iterate and map those objects
        if (list != null) {
            for (Map map : list) {
                try {
                    Object bean = clazz.newInstance();

                    // map HashMap to POJO
                    BeanUtils.populate(bean, map);

                    // add it to list of mapped CDRs
                    mapped.add(bean);

                } catch (Exception ignored) {
                    return null;
                }
            }
        }

        return mapped;
    }
}
