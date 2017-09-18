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

import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 01.06.17
 * Description: Bill creation based on list of charge records
 */
public class Bill {
    private long time_from;
    private long time_to;
    private int run;
    private String account;
    private double charge;
    private String discount;
    private String currency;
    private Object data;

    public Bill(long time_from, long time_to, int run, String account, String currency) {
        this.time_from = time_from;
        this.time_to = time_to;
        this.run = run;
        this.account = account;
        this.currency = currency;
    }

    /**
     * Process charge records
     * @param hierarchy as hashmap or string
     * @param records with charge and account name
     */
    public boolean processChargeBasedOnHierarchy(Object hierarchy, Map<String, List<Charge>> records) {
        // bill for an account is requested
        if (hierarchy instanceof String) {
            processAsString(records.get(hierarchy));
            return true;
        } else if (hierarchy instanceof Map) {
            processAsMap((Map<String, Object>) hierarchy, records);
            return true;
        } else return false;
    }

    /**
     * Apply discount to calculated charge
     * @param percentage discount
     */
    public void applyPercentageDiscount(double percentage) {
        try {
            if (percentage >= 0 && percentage <= 100) {
                setDiscount(String.format("%.2f%%", percentage));
                double discount = (100 - percentage) / 100;
                setCharge(charge * discount);
            }

        } catch (Exception ignored) {}
    }

    /**
     * Process list of records in case that hierarchy is flat (only one account)
     * @param records to process
     */
    private void processAsString(List<Charge> records) {
        BillItem item = new BillItem(records);
        charge = item.getCharge();
        data = item.getData();
    }

    /**
     * Process list of records in case that hierarchy is complex
     * @param hierarchy structure
     * @param records to process
     */
    private void processAsMap(Map<String, Object> hierarchy, Map<String, List<Charge>> records) {
        // the first key in the hashmap is our root
        Map.Entry<String, Object> first = hierarchy.entrySet().iterator().next();
        BillItem root = new BillItem(first.getValue(), records);
        charge = root.getCharge();
        data = root.getData();
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

    public int getRun() {
        return run;
    }
    public void setRun(int run) {
        this.run = run;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public double getCharge() {
        return charge;
    }
    public void setCharge(double charge) {
        this.charge = charge;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    public String getDiscount() {
        return discount;
    }
    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
