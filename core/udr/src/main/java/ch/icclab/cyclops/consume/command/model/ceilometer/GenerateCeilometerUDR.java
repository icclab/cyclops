package ch.icclab.cyclops.consume.command.model.ceilometer;

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.consume.data.model.ceilometer.OpenStackCeilometerUsageUDR;
import ch.icclab.cyclops.consume.data.model.ceilometer.OpenStackCeilometerUsage;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.BatchPointsContainer;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.InfluxDBResponse;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 11/07/16.
 */

public class GenerateCeilometerUDR extends Command {

    final static Logger logger = LogManager.getLogger(GenerateCeilometerUDR.class.getName());

    private InfluxDBClient influxDBClient = new InfluxDBClient();
    private Long from;
    private Long to;

    @Override
    protected Object execute() {

        // Get usage grouped by user and meter where date > hibernate date
        List<OpenStackCeilometerUsage> usage = getUsage(from, to);

        // Compute usage data record depending on the meters
        List<OpenStackCeilometerUsageUDR> udrs = generateUDR(usage);

        // Persist the created UDRs
        BatchPointsContainer container = new BatchPointsContainer();
        for (OpenStackCeilometerUsageUDR udr : udrs) {
            container.addPoint(udr.toPoint());
        }
        influxDBClient.persistContainer(container);

        // And finally broadcast them
        Messenger messenger = Messenger.getInstance();
        messenger.broadcast(udrs);
        return "Ceilometer UDR generated";
    }

    private List<OpenStackCeilometerUsage> getUsage(Long dateFrom, Long dateTo) {
        InfluxDBClient influxDBClient = new InfluxDBClient();
        InfluxDBResponse response = influxDBClient.executeQuery(new QueryBuilder(OpenStackCeilometerUsage.class.getSimpleName())
                .timeFrom(dateFrom, TimeUnit.SECONDS).timeTo(dateTo, TimeUnit.SECONDS));

        try {
            return response.getAsListOfType(OpenStackCeilometerUsage.class);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private List<OpenStackCeilometerUsageUDR> generateUDR(List<OpenStackCeilometerUsage> ceilometerUsage) {
        List<OpenStackCeilometerUsageUDR> udrs = new ArrayList<>();
        Map<String, List<OpenStackCeilometerUsageUDR>> existingCumulatives = new HashMap<>();
        List<OpenStackCeilometerUsageUDR> gaugeUdrs = new ArrayList<>();
        for (OpenStackCeilometerUsage usage : ceilometerUsage) {
            OpenStackCeilometerUsageUDR udr;
            if (usage.getMetadata().get("type").equals("cumulative")) {
                InfluxDBResponse response = influxDBClient.executeQuery(new QueryBuilder(OpenStackCeilometerUsage.class.getSimpleName()).where("account", usage.getAccount()).and("measurementId", usage.getMeasurementId()).beforeTime(usage.getTime(), TimeUnit.SECONDS).orderDesc().limit(1));

                try {
                    List<OpenStackCeilometerUsage> lastUsage = response.getAsListOfType(OpenStackCeilometerUsage.class);

                    if (lastUsage != null && !lastUsage.isEmpty())
                        udr = new OpenStackCeilometerUsageUDR(usage, lastUsage.get(0).getUsage());
                    else
                        // The first import is set as 0 consumption to start the measure from that point
                        udr = new OpenStackCeilometerUsageUDR(usage, 0.0);
                } catch (Exception ignored) {
                    // if mapping failed for some reason, treat it as 0 consumption too
                    udr = new OpenStackCeilometerUsageUDR(usage, 0.0);
                }

                // Add the computed Cumulative UDRs to the map
                udr = processCumulativeUDR(existingCumulatives, udr);
            } else {
                // Add the Gauge UDRs to the ArrayList
                udr = new OpenStackCeilometerUsageUDR(usage);
            }
            udrs.add(udr);
        }
        udrs.addAll(gaugeUdrs);
        return udrs;
    }

    private OpenStackCeilometerUsageUDR processCumulativeUDR(Map<String, List<OpenStackCeilometerUsageUDR>> existingCumulatives, OpenStackCeilometerUsageUDR udr) {
        if (!existingCumulatives.containsKey(udr.getMeasurementId())) {
            List<OpenStackCeilometerUsageUDR> value = new ArrayList<>();
            value.add(udr);
            existingCumulatives.put(udr.getMeasurementId(), value);
        } else {
            List<OpenStackCeilometerUsageUDR> values = existingCumulatives.get(udr.getMeasurementId());
            for (OpenStackCeilometerUsageUDR value : values) {
                udr.setUsage(udr.getUsage() - value.getUsage());
            }
            values.add(udr);
            existingCumulatives.put(udr.getMeasurementId(), values);
        }
        return udr;
    }
}
