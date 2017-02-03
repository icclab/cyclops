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
import ch.icclab.cyclops.util.BeanList;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import org.influxdb.dto.QueryResult;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 09/08/16
 * Description: InfluxDB response container
 */
public class InfluxDBResponse {

    private static String TIME_FIELD = "time";

    private QueryResult object;

    public InfluxDBResponse(QueryResult object) {
        this.object = object;
    }

    /**
     * Output underlying InfluxDB structure in JSON
     * @return flat table structure
     */
    public String getTableAsJson() throws Exception {
        return new Gson().toJson(this.object);

    }

    /**
     * Parse and transform InfluxDB's underlying structure
     * @return JSON
     */
    public String getListOfObjectsAsJson() throws Exception {
        return new Gson().toJson(getListOfObjects());
    }

    /**
     * Parse and transform InfluxDB's underlying structure
     * @param clazz definition
     * @return List<clazz>
     */
    public List getAsListOfType(Class clazz) throws Exception {
        List<Map> list = getListOfObjects();
        return BeanList.populate(list, clazz);
    }

    /**
     * Parse and transform InfluxDB's underlying structure
     * @return list of maps
     */
    public List<Map> getListOfObjects() throws Exception {

        List<Map> list = new ArrayList<>();

        if (object != null && !object.hasError()) {
            // iterate over available results
            for (QueryResult.Result result: object.getResults()) {
                // multiple query responses can have empty results
                if (result != null) {
                    List<QueryResult.Series> bulk = result.getSeries();
                    if (bulk != null && !bulk.isEmpty()) {
                        // iterate over available series
                        for (QueryResult.Series series: result.getSeries()) {
                            // iterate over individual value sets
                            Map<String, String> tags = series.getTags();
                            List<String> columns = series.getColumns();
                            int time = columns.indexOf(TIME_FIELD);
                            List<List<Object>> values = series.getValues();

                            // iterate over individual values
                            if (values != null && !values.isEmpty()) {
                                for (List<Object> value: values) {
                                    // we will store one row here
                                    Map<String, Object> row = new HashMap<>();

                                    // add tag values if there are any
                                    if (tags != null && !tags.isEmpty()) {
                                        row.putAll(tags);
                                    }

                                    // individual entries of the value
                                    int i = 0;
                                    for (Object entry: value) {
                                        // timestamp needs to be in Long and not String
                                        if (time == i) {
                                            entry = new DateTime(entry).getMillis() / 1000;
                                        }

                                        row.put(columns.get(i), entry);
                                        i++;
                                    }

                                    // remove property that is used for counting and should never be visible
                                    row.remove(InfluxDBCredentials.COUNTER_FIELD_NAME);

                                    // return de-flattened map
                                    list.add(new Gson().fromJson(JsonUnflattener.unflatten(new Gson().toJson(row)), Map.class));
                                }
                            }
                        }
                    }
                }
            }

            return list;
        }

        throw new Exception("Couldn't parse InfluxDB response");
    }
}
