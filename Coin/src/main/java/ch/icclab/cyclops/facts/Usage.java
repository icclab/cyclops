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

import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 15.05.17
 * Description: Usage data coming from UDR microservice
 */
public class Usage extends MappedFact {
    public static String USAGE_FIELD = "usage";

    private String metric;
    private String account;
    private double usage;
    private long time_from;
    private long time_to;
    private Map<String, Object> data;
    private String unit;
    /**
     * Empty constructor necessary for JSON mapping
     */
    public Usage() {
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

    public double getUsage() {
        return usage;
    }
    public void setUsage(double usage) {
        this.usage = usage;
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

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
