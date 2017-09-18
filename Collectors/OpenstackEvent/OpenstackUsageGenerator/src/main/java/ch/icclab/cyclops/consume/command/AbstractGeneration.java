package ch.icclab.cyclops.consume.command;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.consume.command.generation.usage.Usage;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;

import ch.icclab.cyclops.util.loggers.CommandLogger;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Created on: 27-05-17
 * Description: Abstract runner for our schedulers
 */

public abstract class AbstractGeneration extends Command {

    //link to influxDB client
    protected static DbAccess db = new DbAccess();
    //Openstack settings
    protected static Settings settings = Loader.getSettings();
    //Status object
    protected Status status = new Status();

    // from and to time
    protected Long time_from;
    protected Long time_to;

    //generation step
    private int step;

    //step to seconds
    protected int second_step;

    //fast source list get
    protected boolean fast;

    //custom usage POJO
    protected Class usageClass = getUsageFormat();


    public abstract Class getUsageFormat();

    public abstract ArrayList<Usage> generateUsageRecords();

    @Override
    Status execute(){
        second_step = step * 1000;

        // sanity checks first
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) {
            status.setClientError("Invalid FROM and TO (unit to be in milliseconds)");
            return status;
        }
        Integer pageLimit = settings.getDatabaseCredentials().getDatabasePageLimit();
        List<Usage> eventList = generateUsageRecords();
        if (status.hadClientError() || status.hadServerError()) return status;
        List<List<Usage>> partedEventList = Lists.partition(eventList, pageLimit);
        CommandLogger.log(String.format("From %s to %s %s are splitted into %s chunks",
                time_from, time_to, usageClass.getSimpleName(), partedEventList.size()));
        String message;
        if (!partedEventList.isEmpty()) {
            for (List<Usage> singleEventList: partedEventList){
                if (Messenger.broadcast(singleEventList)){
                    CommandLogger.log(String.format("%s %s usages are successfully sent.",
                            singleEventList.size(), usageClass.getSimpleName()));
                } else {
                    message = String.format("Some records from %s to %s %s are not sent",
                            time_from, time_to, usageClass.getSimpleName());
                    CommandLogger.log(message);
                    status.setClientError(message);
                    return status;
                }
            }
            message = String.format("All records from %s to %s %s are successfully sent.",
                    time_from, time_to, usageClass.getSimpleName());
            CommandLogger.log(message);
            status.setSuccessful(message);
            return status;
        }

        message = String.format("There are not records from %s to %s at %s. Nothing send to Rabbitmq.",
                    time_from, time_to, usageClass.getSimpleName());
        CommandLogger.log(message);
        status.setSuccessful(message);

        return status;
    }
}

