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
import ch.icclab.cyclops.consume.command.model.generic.model.UDR;
import ch.icclab.cyclops.consume.command.model.generic.model.UsageData;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atteo.evo.inflector.English;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: Command representing request for Events -> UDRs generation
 */
public class GenerateUDR extends Command {
    final static Logger logger = LogManager.getLogger(GenerateUDR.class.getName());

    private Long from;
    private Long to;

    // notify subscribed parties
    private boolean broadcast;

    // route message via dispatch
    private boolean dispatch;

    // output UDRs synchronously
    private boolean output;

    public static class Measurement {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Validate request and create UDR records for individual accounts
     * @return string confirmation message
     */
    @Override
    protected Object execute() {

        // sanity checks first
        if (from == null || to == null || from < 0l || to <= from) {
            return "[ERROR] invalid FROM and TO";
        }

        // get list of measurements
        List<String> measurements = getListOfMeasurements();
        if (measurements != null && !measurements.isEmpty()) {

            // flat container
            List<UsageData> usageData = new ArrayList<>();

            // query all measurements and get Usage Data
            measurements.stream().forEach(measurement -> usageData.addAll(queryMeasurement(measurement)));

            if (!usageData.isEmpty()) {
                // downsample by summing up streams and generate UDR records
                List<UDR> UDRs = downsampleAndGenerate(usageData);

                // persist them to database and output status code
                persistUDRs(UDRs);

                // broadcast if desired
                if (broadcast) {
                    Messenger.getInstance().broadcast(UDRs);
                }

                // dispatch if desired
                if (dispatch) {
                    Messenger.getInstance().publish(UDRs, getClass().getSimpleName());
                }

                // either return created UDRs or output a message
                return (output)? UDRs : String.format("%d %s generated", UDRs.size(), English.plural("UDR", UDRs.size()));

            } else {
                return "No data";
            }

        } else {
            return "No data";
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
            return measurements.stream().map(Measurement::getName).filter(name -> !name.equals(UDR.class.getSimpleName())).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Query database and list usage data for specified measurement
     * @param measurement to query
     * @return UsageData list
     */
    private List<UsageData> queryMeasurement(String measurement) {
        try {
            // construct and execute query
            QueryBuilder query = new QueryBuilder(measurement).timeFrom(from, TimeUnit.SECONDS).timeTo(to, TimeUnit.SECONDS);
            InfluxDBResponse response = new InfluxDBClient().executeQuery(query);

            // parse as list of UsageData
            return response.getAsListOfType(UsageData.class);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    /**
     * Go over list of Usage Data, downsample it and generate UDRs
     * @param data to be processed
     * @return list of UDRs
     */
    private List<UDR> downsampleAndGenerate(List<UsageData> data) {

        Map<String, List> accounts = new HashMap<>();

        // go over individual items and downsample them
        for (UsageData item : data) {

            // container for account data
            List<UsageData> accountData = accounts.getOrDefault(item.getAccount(), new ArrayList<>());

            // find index of the item
            Integer index = accountData.indexOf(item);
            if (index >= 0) {
                // update original record
                accountData.get(index).addToUsage(item.getUsage());
            } else {
                // add record to the list
                accountData.add(item);
            }

            // put it back
            accounts.put(item.getAccount(), accountData);
        }

        // now that we are done, let's create UDR records
        List<UDR> UDRs = new ArrayList<>();
        for (Map.Entry<String, List> entry: accounts.entrySet()) {
            UDR udr = new UDR(entry.getKey(), from, to);
            udr.setData(entry.getValue());
            UDRs.add(udr);
        }

        return UDRs;
    }

    /**
     * Persist UDR records into database (synchronously)
     * @param UDRs to be persisted
     */
    private void persistUDRs(List<UDR> UDRs) {
        // prepare container
        BatchPointsContainer container = new BatchPointsContainer();

        // fill container with individual points
        UDRs.stream().forEach(udr -> container.addPoint(GenerateDBPoint.fromObjectWithTimeAndTags(udr, UDR.getTimeFieldName(), UDR.getTimeUnit(), UDR.getTagNames())));

        // persist container
        new InfluxDBClient().persistContainer(container);
    }
}
