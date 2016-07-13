package ch.icclab.cyclops.consume.command.model;

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.consume.data.model.ceilometer.OpenStackCeilometerUDR;
import ch.icclab.cyclops.consume.data.model.ceilometer.OpenStackCeilometerUsage;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.BatchPointsContainer;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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

    private InfluxDBClient influxDBClient = InfluxDBClient.getInstance();
    private Long from;
    private Long to;

    @Override
    protected void execute() {

        // Get usage grouped by user and meter where date > hibernate date
        List<OpenStackCeilometerUsage> usage = getUsage(from, to);

        // Compute usage data record depending on the meters
        List<OpenStackCeilometerUDR> udrs = generateUDR(usage);

        // Persist the created UDRs
        BatchPointsContainer container = new BatchPointsContainer();
        for (OpenStackCeilometerUDR udr : udrs) {
            container.addPoint(udr.toPoint());
        }
        influxDBClient.persistContainer(container);

        // And finally broadcast them
        Messenger messenger = Messenger.getInstance();
        messenger.broadcast(udrs);
    }

    private List<OpenStackCeilometerUsage> getUsage(Long dateFrom, Long dateTo) {
        InfluxDBClient influxDBClient = InfluxDBClient.getInstance();
        List<OpenStackCeilometerUsage> usage = influxDBClient.executeQueryAndMapItToClass(new QueryBuilder(OpenStackCeilometerUsage.class.getSimpleName())
                .timeFrom(dateFrom, TimeUnit.SECONDS).timeTo(dateTo, TimeUnit.SECONDS), OpenStackCeilometerUsage.class);

        return usage;
    }

    private List<OpenStackCeilometerUDR> generateUDR(List<OpenStackCeilometerUsage> ceilometerUsage) {
        List<OpenStackCeilometerUDR> udrs = new ArrayList<>();
        Map<String, List<OpenStackCeilometerUDR>> existingCumulatives = new HashMap<>();
        List<OpenStackCeilometerUDR> gaugeUdrs = new ArrayList<>();
        for (OpenStackCeilometerUsage usage : ceilometerUsage) {
            OpenStackCeilometerUDR udr;
            if (usage.getMetadata().get("type").equals("cumulative")) {
                List<OpenStackCeilometerUsage> lastUsage = influxDBClient.executeQueryAndMapItToClass(new QueryBuilder(OpenStackCeilometerUsage.class.getSimpleName()).where("account", usage.getAccount()).and("meter_name", usage.getMeter_name()).beforeTime(usage.getTime(), TimeUnit.SECONDS).orderDesc().limit(1), OpenStackCeilometerUsage.class);

                if (lastUsage != null && !lastUsage.isEmpty())
                    udr = new OpenStackCeilometerUDR(usage, lastUsage.get(0).getUsage());
                else
                    // The first import is set as 0 consumption to start the measure from that point
                    udr = new OpenStackCeilometerUDR(usage, 0.0);

                // Add the computed Cumulative UDRs to the map
                udr = processCumulativeUDR(existingCumulatives, udr);
            } else {
                // Add the Gauge UDRs to the ArrayList
                udr = new OpenStackCeilometerUDR(usage);
            }
            udrs.add(udr);
        }
        udrs.addAll(gaugeUdrs);
        return udrs;
    }

    private OpenStackCeilometerUDR processCumulativeUDR(Map<String, List<OpenStackCeilometerUDR>> existingCumulatives, OpenStackCeilometerUDR udr) {
        if (!existingCumulatives.containsKey(udr.getMeter_name())) {
            List<OpenStackCeilometerUDR> value = new ArrayList<>();
            value.add(udr);
            existingCumulatives.put(udr.getMeter_name(), value);
        } else {
            List<OpenStackCeilometerUDR> values = existingCumulatives.get(udr.getMeter_name());
            for (OpenStackCeilometerUDR value : values) {
                udr.setUsage(udr.getUsage() - value.getUsage());
            }
            values.add(udr);
            existingCumulatives.put(udr.getMeter_name(), values);
        }
        return udr;
    }
}
