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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.publish.Messenger;

import java.util.*;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 29.05.17
 * Description: Command for bill generation
 */
public class GenerateBill extends Command {
    // mandatory fields
    private Long time_from;
    private Long time_to;
    private Integer run;
    private Object request;

    private class FlushCDRs {
        private String command;
        private Long time_from;
        private Long time_to;
        private int run;
        private List accounts;

        public FlushCDRs(Long time_from, Long time_to, int run, List accounts) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
            this.run = run;
            this.accounts = accounts;
        }

        public String getCommand() {
            return command;
        }
        public Long getTime_from() {
            return time_from;
        }
        public Long getTime_to() {
            return time_to;
        }
        public int getRun() {
            return run;
        }
        public List getAccounts() {
            return accounts;
        }
    }
    private class BillRequest {
        private String type;
        private Long time_from;
        private Long time_to;
        private int run;
        private List<String> accounts;
        private Object hierarchy;

        public BillRequest(Long time_from, Long time_to, int run, List<String> accounts, Object hierarchy) {
            this.type = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
            this.run = run;
            this.accounts = accounts;
            this.hierarchy = hierarchy;
        }

        public String getType() {
            return type;
        }
        public Long getTime_from() {
            return time_from;
        }
        public Long getTime_to() {
            return time_to;
        }
        public int getRun() {
            return run;
        }
        public List<String> getAccounts() {
            return accounts;
        }
        public Object getHierarchy() {
            return hierarchy;
        }
    }
    private class RoutingKeys {
        private String PublishToCDRWithKey = "CDR";
        private String PublishToCoinBillWithKey = "CoinBill";

        public RoutingKeys() {
        }

        public String getPublishToCDRWithKey() {
            return PublishToCDRWithKey;
        }
        public void setPublishToCDRWithKey(String publishToCDRWithKey) {
            PublishToCDRWithKey = publishToCDRWithKey;
        }

        public String getPublishToCoinBillWithKey() {
            return PublishToCoinBillWithKey;
        }
        public void setPublishToCoinBillWithKey(String publishToCoinBillWithKey) {
            PublishToCoinBillWithKey = publishToCoinBillWithKey;
        }
    }

    public GenerateBill() {
        this.setCommand(this.getClass().getSimpleName());
    }

    public GenerateBill(Long time_from, Long time_to, Integer run, Object request) {
        this.setCommand(this.getClass().getSimpleName());
        this.time_from = time_from;
        this.time_to = time_to;
        this.run = run;
        this.request = request;
    }

    @Override
    Status execute() {
        Status status = new Status();

        // sanity checks first
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) {
            status.setClientError("Invalid FROM and TO (unit to be in milliseconds)");
            return status;
        }

        try {
            // validate request and get list of records
            List<String> accounts = validateRequestBody(request);

            // request CDR to flush data for these accounts
            FlushCDRs flushCDRs = new FlushCDRs(time_from, time_to, run, accounts);
            BillRequest billRequest = new BillRequest(time_from, time_to, run, accounts, request);

            // extract routing keys from the configuration file
            RoutingKeys keys = Loader.extractProperties(RoutingKeys.class);
            // and also forward this request to CoinBill
            if (Messenger.publish(flushCDRs, keys.getPublishToCDRWithKey()) && Messenger.publish(billRequest, keys.getPublishToCoinBillWithKey()))
                status.setSuccessful(String.format("GenerateBill request processed for %d - %d", time_from, time_to));
            else status.setServerError(String.format("GenerateBill unable to notify CDR and CoinBill for %d - %d due to RabbitMQ", time_from, time_to));
        } catch (Exception e) {
            status.setClientError(e.getMessage());
        }

        return status;
    }

    /**
     * Validate the structure of the organization
     * @param request body
     * @return List of accounts or null
     * @throws Exception with error message
     */

    private List<String> validateRequestBody(Object request) throws Exception {
        if (request == null) throw new Exception("Nonexistent REQUEST field");
        else if (request instanceof List) throw new Exception("List of accounts not allowed, either provide only one account (String) or a hierarchy (Map)");
        else if (request instanceof Map) {
            Map map = (Map) request;
            if (map.size() != 1) throw new Exception("Only one top level organization allowed");
            else {
                List<String> accounts = traverseAndValidate(map);
                if (accounts == null || accounts.isEmpty()) throw new Exception("Invalid REQUEST hierarchy");
                else return accounts;
            }
        } else if (!(request instanceof String) || ((String) request).isEmpty()) throw new Exception("Invalid REQUEST body");
        else return Collections.singletonList((String) request);
    }

    /**
     * Traverse and validate the content of the Map
     * @param map from the request body
     * @return List of accounts or null
     */
    private List<String> traverseAndValidate(Map map) {
        List<String> accounts = new ArrayList<>();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            // only string key allowed
            if (!(pair.getKey() instanceof String) || ((String) pair.getKey()).isEmpty()) return null;

            // now validate the value
            Object value = pair.getValue();
            if (value == null) return null;
            else if (value instanceof List) {
                List list = (List) value;

                // go over individual entries in the list
                // we only allow Strings and nested Maps
                for (Object entry: list) {
                    if (entry == null) return null;
                    else if (entry instanceof List) return null;
                    else if (entry instanceof Map) {
                        List<String> tmp = traverseAndValidate((Map) entry);
                        if (tmp == null || tmp.isEmpty()) return null;
                        else accounts.addAll(tmp);
                    } else if (!(entry instanceof String) || ((String) entry).isEmpty()) return null;
                    else accounts.add((String) entry);
                }
            } else if (value instanceof Map) {
                List<String> tmp = traverseAndValidate((Map) value);
                if (tmp == null || tmp.isEmpty()) return null;
                else accounts.addAll(tmp);
            } else if (!(value instanceof String) || ((String) value).isEmpty()) return null;
        }

        // if our temporary list of accounts is not empty, return it
        return (accounts.isEmpty())? null: accounts;
    }
}
