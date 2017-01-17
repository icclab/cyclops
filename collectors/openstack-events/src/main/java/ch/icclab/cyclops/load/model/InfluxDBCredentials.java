package ch.icclab.cyclops.load.model;
/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: InfluxDB credentials and settings
 */
public class InfluxDBCredentials {

    public static String COUNTER_FIELD_NAME = "metaAckAndVal";
    public static String DEFAULT_DATABASE_NAME = "cyclops.box";
    public static String DEFAULT_MEASUREMENT_NAME = "unknown";
    public static Integer DEFAULT_PAGE_SIZE_LIMIT = 500;
    public static Integer DEFAULT_QUERY_TIMEOUT = 10;


    private String influxDBURL;
    private String influxDBUsername;
    private String influxDBPassword;
    private String influxDBTSDB;
    private String influxDBDefaultMeasurement;
    private Integer influxDBPageSizeLimit;
    private Integer influxDBQueryTimeout;


    public String getInfluxDBURL() {
        return influxDBURL;
    }
    public void setInfluxDBURL(String influxDBURL) {
        this.influxDBURL = influxDBURL;
    }

    public String getInfluxDBUsername() {
        return influxDBUsername;
    }
    public void setInfluxDBUsername(String influxDBUsername) {
        this.influxDBUsername = influxDBUsername;
    }

    public String getInfluxDBPassword() {
        return influxDBPassword;
    }
    public void setInfluxDBPassword(String influxDBPassword) {
        this.influxDBPassword = influxDBPassword;
    }

    public String getInfluxDBTSDB() {
        return influxDBTSDB;
    }
    public void setInfluxDBTSDB(String influxDBTSDB) {
        this.influxDBTSDB = influxDBTSDB;
    }

    public String getInfluxDBDefaultMeasurement() {
        return influxDBDefaultMeasurement;
    }
    public void setInfluxDBDefaultMeasurement(String influxDBDefaultMeasurement) {
        this.influxDBDefaultMeasurement = influxDBDefaultMeasurement;
    }

    public Integer getInfluxDBPageSizeLimit() {
        return influxDBPageSizeLimit;
    }
    public void setInfluxDBPageSizeLimit(Integer influxDBPageSizeLimit) {
        this.influxDBPageSizeLimit = influxDBPageSizeLimit;
    }

    public Integer getInfluxDBQueryTimeout() {
        return influxDBQueryTimeout;
    }
    public void setInfluxDBQueryTimeout(Integer influxDBQueryTimeout) {
        this.influxDBQueryTimeout = influxDBQueryTimeout;
    }
}