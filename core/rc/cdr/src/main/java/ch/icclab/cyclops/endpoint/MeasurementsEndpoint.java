package ch.icclab.cyclops.endpoint;
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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.PrettyGson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 17/05/16
 * Description: Endpoint for measurements
 */
public class MeasurementsEndpoint extends ServerResource{
    final static Logger logger = LogManager.getLogger(MeasurementsEndpoint.class.getName());

    public static String ENDPOINT = "/measurements";

    private final APICallCounter counter = APICallCounter.getInstance();
    private final InfluxDBClient dbClient = InfluxDBClient.getInstance();

    @Get
    public String processGet(){
        counter.increment(ENDPOINT);

        // we will list measurements
        QueryBuilder builder = QueryBuilder.getMeasurementsQuery();

        // get list of measurements
        List<Map> result = dbClient.executeQuery(builder);

        List<String> list = new ArrayList<>();

        // iterate over list of maps and add to the list only names
        for (Map map: result) {
            if (map.containsKey(InfluxDBClient.MEASUREMENT_FIELD_NAME)) {
                String name = (String) map.get(InfluxDBClient.MEASUREMENT_FIELD_NAME);

                // we are returning only measurement names that are not the default container
                if (!name.equals(Loader.getSettings().getInfluxDBCredentials().getInfluxDBDefaultMeasurement())) {
                    list.add(name);
                }
            }
        }

        // return as JSON
        return PrettyGson.toJson(list);
    }
}
