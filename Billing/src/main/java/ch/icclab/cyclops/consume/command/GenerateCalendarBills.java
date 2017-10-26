package ch.icclab.cyclops.consume.command;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import ch.icclab.cyclops.dao.BillRun;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 05.09.17
 * Description:
 */
public class GenerateCalendarBills extends Command{
    private String today;

    private HashMap<String, List<Object>> schedule;

    private transient DateTime now = new DateTime().withTimeAtStartOfDay();
    private transient List<Object> monthly;
    private transient List<Object> quarterly;
    private transient List<Object> halfyearly;
    private transient List<Object> yearly;

    /**
     * Routing key for dispatching commands to itself
     */
    private class RoutingKey {
        private String PublishToSelf = "SelfPublish";

        public RoutingKey() {
        }

        public String getPublishToSelf() {
            return PublishToSelf;
        }

        public void setPublishToSelf(String publishToSelf) {
            PublishToSelf = publishToSelf;
        }
    }

    @Override
    Status execute() {
        Status status = new Status();

        try {
            // replace now reference with the specified date
            now = DateTime.parse(today).withTimeAtStartOfDay();
        } catch (Exception ignored) {}

        // extract monthly, quarterly, half-yearly and yearly list of resellers from the schedule
        if (!extractSchedule(schedule)) status.setClientError("Invalid request, nothing to process");
        else {
            // monthly, quarterly, half-yearly and yearly bill requests
            List<GenerateBillRun> requests = getBillRequests();

            if (requests == null) {
                status.setServerError("Couldn't generate list of Bill Request commands");
            } else if (requests.isEmpty()) {
                status.setSuccessful("Nothing to generate for today");
            } else {
                RoutingKey key = Loader.extractProperties(RoutingKey.class);
                for (Command command: requests) {
                    if (!Messenger.publish(command, key.getPublishToSelf())) {
                        status.setServerError("Couldn't publish one of generated commands to the RabbitMQ");
                        return status;
                    }
                }

                status.setSuccessful(String.format("Created and published %d Bill Request commands", requests.size()));
            }
        }

        return status;
    }

    /**
     * Extract monthly, quarterly, half-yearly and yearly list of resellers from the schedule
     * @return is there something to process
     */
    private boolean extractSchedule(HashMap<String, List<Object>> schedule) {
       boolean somethingToProcess = false;

       if (schedule == null || schedule.isEmpty()) return false;
       else {
           if (schedule.containsKey("monthly")) {
               monthly = schedule.get("monthly");
               somethingToProcess = true;
           }

           if (schedule.containsKey("quarterly")) {
               quarterly = schedule.get("quarterly");
               somethingToProcess = true;
           }

           if (schedule.containsKey("halfyearly")) {
               halfyearly = schedule.get("halfyearly");
               somethingToProcess = true;
           }

           if (schedule.containsKey("yearly")) {
               yearly = schedule.get("yearly");
               somethingToProcess = true;
           }

           return somethingToProcess;
       }
    }

    /**
     * Get list of GenerateBill command requests
     * @return list or null
     */
    private List<GenerateBillRun> getBillRequests() {
        List<GenerateBillRun> requests = new ArrayList<>();

        try {
            // is it first of the month
            if (now.dayOfMonth().get() == 1) {
                // end of the requested window
                DateTime end = now.minusMillis(1);

                // initialize bill run and get its ID
                BillRun run = new BillRun();
                DbAccess.DatabaseResult result = new DbAccess().storePersistentObjectReturningId(run, BillRun.ID_FIELD, BillRun.class);
                if (result.getPersistenceStatus() != DbAccess.PersistenceStatus.OK) return null;
                run = (BillRun) result.getReturnValue();

                // process monthly bills
                requests.addAll(createGenerateBillCommands(monthly, now.minusMonths(1), end, run.getId()));

                int thisMonth = now.monthOfYear().get();

                // process quarterly bills
                if (thisMonth == 1 || thisMonth == 4 || thisMonth == 7 || thisMonth == 10)
                    requests.addAll(createGenerateBillCommands(quarterly, now.minusMonths(3), end, run.getId()));

                // process half yearly bills
                if (thisMonth == 1 || thisMonth == 7) requests.addAll(createGenerateBillCommands(halfyearly, now.minusMonths(6), end, run.getId()));

                // process yearly bills
                if (thisMonth == 1) requests.addAll(createGenerateBillCommands(yearly, now.minusMonths(12), end, run.getId()));
            }

            return requests;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Create GenerateBill commands
     * @param list of request bodies
     * @param start date
     * @param end date
     * @return list
     */
    private List<GenerateBillRun> createGenerateBillCommands(List<Object> list, DateTime start, DateTime end, int billRun) {
        List<GenerateBillRun> requests = new ArrayList<>();

        if (list != null && !list.isEmpty()) list.forEach(item -> requests.add(new GenerateBillRun(start.getMillis(), end.getMillis(), billRun, item)));

        return requests;
    }
}
