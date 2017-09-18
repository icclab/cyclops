package ch.icclab.cyclops.facts;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 01.06.17
 * Description: Bill request coming from the Billing microservice
 */
public class BillRequest extends TypedFact {
    private long time_from;
    private long time_to;
    private int run;
    private List<String> accounts;
    private Object hierarchy;

    public BillRequest() {
    }

    /**
     * No CDR list is present, create an empty bill
     * @param currency as default
     * @return list of empty bills
     */
    public Bill process(String currency) {
        // determine account name (either complex HashMap, or just String)
        String account = (hierarchy instanceof String)? (String) hierarchy : (String) ((Map) hierarchy).keySet().toArray()[0];

        // an empty bill
        return new Bill(time_from, time_to, run, account, currency);
    }

    /**
     * Process list of CDRs and add them to the hierarchy
     * @param CDRs containing charge for multiple users
     * @return list of bills
     */
    public List<Bill> process(List<Charge> CDRs) {
        List<Bill> bills = new ArrayList<>();

        // first split list of CDRs based on accounts and currency
        Map<String, Map<String, List<Charge>>> currencies = CDRs.stream().collect(Collectors.groupingBy(Charge::getCurrency,Collectors.groupingBy(Charge::getAccount)));

        // determine account name (either complex HashMap, or just String)
        String account = (hierarchy instanceof String)? (String) hierarchy : (String) ((Map) hierarchy).keySet().toArray()[0];

        // prepare bills
        for (Map.Entry<String, Map<String, List<Charge>>> entry: currencies.entrySet()) {
            Bill bill = new Bill(time_from, time_to, run, account, entry.getKey());

            // process map of accounts and their charge records
            boolean status = bill.processChargeBasedOnHierarchy(hierarchy, entry.getValue());

            // add the processed bill
            if (status) bills.add(bill);
        }

        return bills;
    }

    //========== Getters and setters
    public long getTime_from() {
        return time_from;
    }
    public void setTime_from(long time_from) {
        this.time_from = time_from;
    }

    public long getTime_to() {
        return time_to;
    }
    public void setTime_to(long time_to) {
        this.time_to = time_to;
    }

    public List<String> getAccounts() {
        return accounts;
    }
    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public Object getHierarchy() {
        return hierarchy;
    }
    public void setHierarchy(Object hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getRun() {
        return run;
    }
    public void setRun(int run) {
        this.run = run;
    }
}
