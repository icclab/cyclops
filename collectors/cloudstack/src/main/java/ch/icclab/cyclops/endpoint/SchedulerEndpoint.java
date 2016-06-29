/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

package ch.icclab.cyclops.endpoint;

import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: This class handles the internal scheduler for RCB
 */
public class SchedulerEndpoint extends ServerResource {
    final static Logger logger = LogManager.getLogger(SchedulerEndpoint.class.getName());

    public static String ENDPOINT = "/scheduler";
    public static String ATTRIBUTE = "command";

    // We are going to be working with CloudStack scheduler here
    private static Scheduler scheduler = Scheduler.getInstance();

    // Command that was received via API call
    private String command;

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * This method is invoked in order to get command from API URL
     */
    public void doInit() {
        command = (String) getRequestAttributes().get(ATTRIBUTE);
    }

    /**
     * This method will respond to API call
     * @return return message
     */
    @Get
    public String processCommand() {
        logger.trace("Received API command: " + command);
        SchedulerLogger.log(String.format("Scheduler command: %s", command));

        counter.increment(ENDPOINT);

        // what to do? determine based on command

        if (command.equalsIgnoreCase("start")) {

            if (scheduler.isRunning()) {
                return "Scheduler has already been started";
            } else {
                scheduler.start();

                return "Scheduler started";
            }

        } else if (command.equalsIgnoreCase("restart")) {

            scheduler.stop();
            scheduler.start();

            return "Scheduler restarted";

        } else if (command.equalsIgnoreCase("stop")) {

            if (scheduler.isRunning()) {
                scheduler.stop();

                return "Scheduler stopped";
            } else {
                return "Scheduler was not running";
            }

        } else if (command.equalsIgnoreCase("status")) {

            return (scheduler.isRunning()) ? "running" : "stopped";

        } else if (command.equalsIgnoreCase("force")) {

            scheduler.force();

            return "Records pulled manually";
        } else {
            return "Unsupported command - use either start, restart, stop, status or force";
        }
    }
}
