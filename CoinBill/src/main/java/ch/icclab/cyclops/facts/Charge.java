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

import org.apache.commons.lang.math.NumberUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 15.05.17
 * Description: Charge data based on Usage data
 */
public class Charge extends MappedFact {
    public static String CHARGE_FIELD = "charge";

    private String metric;
    private String account;
    private double charge;
    private long time_from;
    private long time_to;
    private Integer run = 0;
    private Map<String, Object> data;
    private String currency;

    public Charge() {
    }

    public Charge(Usage usage) {
        metric = usage.getMetric();
        account = usage.getAccount();
        time_from = usage.getTime_from();
        time_to = usage.getTime_to();

        // copy metadata
        data = usage.getData();

        // put usage into data map
        addToData("usage", usage.getUsage());

        // also include unit if available
        addToData("unit", usage.getUnit());
    }

    /**
     * Add value to data map
     * @param key string (not null)
     * @param value object (not null)
     * @return status of the operation
     */
    public boolean addToData(String key, Object value) {
        if (key != null && !key.isEmpty() && value != null && value instanceof Serializable) {

            // allocate if not available yet
            if (data == null) data = new HashMap<>();

            data.put(key, value);
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
                addToData("customer_discount", String.format("%.2f%%", percentage));
                double discount = (100 - percentage) / 100;
                setCharge(charge * discount);
            }

        } catch (Exception ignored) {}
    }

    /**
     * Apply product discount presumably present in data.discount
     */
    public void applyProductDiscountIfPresent() {
        try {
            double percentage = (double) data.get("discount");
            if (percentage >= 0 && percentage <= 100) {
                addToData("product_discount", String.format("%.2f%%", percentage));
                double discount = (100 - percentage) / 100;
                setCharge(charge * discount);
                data.remove("discount");
            }

        } catch (Exception ignored) {}
    }

    //========== Getters and setters
    public String getMetric() {
        return metric;
    }
    public void setMetric(String metric) {
        this.metric = metric;
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

    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getRun() {
        return run;
    }
    public void setRun(Integer run) {
        this.run = run;
    }
}
