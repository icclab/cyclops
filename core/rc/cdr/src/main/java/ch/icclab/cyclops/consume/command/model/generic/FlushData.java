package ch.icclab.cyclops.consume.command.model.generic;
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

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.consume.command.model.generic.model.CDR;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.publish.APICaller;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.InfluxDBResponse;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.loggers.CommandLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: Skoviera
 * Created: 09/09/16
 * Description: Flush data from database
 */
public class FlushData extends Command{

    private class URLs {
        String BillingRuleEngineURL;

        public URLs() {
        }

        public boolean isValid() {
            return BillingRuleEngineURL != null && !BillingRuleEngineURL.isEmpty();
        }
    }

    // mandatory
    private Long from;
    private Long to;

    // by default we are synchronously pushing to the next step
    private Boolean sync;

    // in case we want data set be returned
    private Boolean output;

    // optional
    private List<String> accounts;

    public static class Measurement {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    protected Object execute() {
        try {
            // sanity checks first
            if (from == null || to == null || from < 0l || to <= from) {
                return "[ERROR] invalid FROM and TO";
            }

            // get list of measurements
            List<String> measurements = getListOfMeasurements();
            if (measurements != null && !measurements.isEmpty()) {
                List<CDR> list = new ArrayList<>();

                // query all measurements and get Usage Data
                measurements.stream().forEach(measurement -> list.addAll(queryMeasurement(measurement)));

                // we got some results from database
                if (!list.isEmpty()) {

                    // do we want to push it to the next micro service or not
                    if (sync == null || sync) {
                        // load URLS from configuration file
                        URLs urls = Loader.extractProperties(URLs.class);

                        if (urls != null && urls.isValid()) {
                            CommandLogger.log(String.format("Flushing data (%d items) to Billing Rule engine (%s)", list.size(), urls.BillingRuleEngineURL));

                            new APICaller().post(new URL(String.format("http://%s/ruleengine/facts", urls.BillingRuleEngineURL)), list);
                        } else {
                            return "Check configuration file, command endpoints are not valid";
                        }
                    }

                    // should we simply return the data set?
                    return (output != null && output)? list: String.format("Flushed %d items", list.size());
                } else {
                    CommandLogger.log("No data to be pushed into Billing Rule engine, as list of records received from underlying database is zero");
                    return "No data in measurements";
                }
            } else {
                return "No measurements";
            }

        } catch (Exception e) {
            // something required was not provided, do nothing
            return String.format("[ERROR] %s", e.getMessage());
        }
    }

    /**
     * Get list of measurements
     * @return list of null
     */
    private List<String> getListOfMeasurements(){
        try {
            // prepare and execute query
            QueryBuilder builder = QueryBuilder.getMeasurementsQuery();
            InfluxDBResponse result = new InfluxDBClient().executeQuery(builder);

            // map response to Measurement class
            List<Measurement> measurements = result.getAsListOfType(Measurement.class);

            // get it as a list of Strings and filter out UDR measurement
            return measurements.stream().map(Measurement::getName).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Query database and list usage data for specified measurement
     * @param measurement to query
     * @return CDR list
     */
    private List<CDR> queryMeasurement(String measurement) {
        try {

            List<QueryBuilder> queries = new ArrayList<>();

            // if there are multiple accounts
            if (accounts != null && !accounts.isEmpty()) {
                for (String account: accounts) {
                    queries.add(new QueryBuilder(measurement).timeFrom(from, TimeUnit.SECONDS).timeTo(to, TimeUnit.SECONDS).where("account", account));
                }
            } else {
                queries.add(new QueryBuilder(measurement).timeFrom(from, TimeUnit.SECONDS).timeTo(to, TimeUnit.SECONDS));
            }

            InfluxDBResponse response = new InfluxDBClient().executeQuery(queries);

            // parse as list of UsageData
            return response.getAsListOfType(CDR.class);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
}
