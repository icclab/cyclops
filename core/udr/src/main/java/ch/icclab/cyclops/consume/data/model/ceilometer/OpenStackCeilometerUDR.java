package ch.icclab.cyclops.consume.data.model.ceilometer;

import org.influxdb.dto.Point;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.HashMap;
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

public class OpenStackCeilometerUDR {
    private String _class = this.getClass().getSimpleName();
    private long time;
    private String account;
    private double usage;
    private String meter_name;
    private String type;
    private String unit;

    public OpenStackCeilometerUDR(OpenStackCeilometerUsage usageData) {
        // Constructor for Gauge, Delta Meters or first cumulative measurement
        this.time = usageData.getTime();
        this.account = usageData.getAccount();
        this.meter_name = usageData.getMeter_name();
        this.usage = usageData.getUsage();
        this.type = (String) usageData.getMetadata().get("type");
        this.unit = (String) usageData.getMetadata().get("unit");
    }

    public OpenStackCeilometerUDR(OpenStackCeilometerUsage usageData, Double usage) {
        // Constructor for Cumulative Meters
        this.time = usageData.getTime();
        this.account = usageData.getAccount();
        this.meter_name = usageData.getMeter_name();
        this.type = (String) usageData.getMetadata().get("type");
        this.unit = (String) usageData.getMetadata().get("unit");

        if (usageData.getUsage() == usage || usage == 0) {
            // In case that the Usage is the same than the last Ceilometer Usage, the consumption has been 0
            this.usage = 0;
        }else{
            this.usage = usageData.getUsage() - usage;
        }
    }

    /**
     * This public method will access data and create db Point
     *
     * @return db point
     */
    public Point.Builder toPoint() {
        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        return Point.measurement(this._class)
                .time(time, TimeUnit.SECONDS)
                .tag(tags)
                .fields(fields);
    }

    /**
     * Get tags for point generation
     *
     * @return hashmap
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("account", account);
        map.put("meter_name", meter_name);

        return map;
    }

    /**
     * Get fields for point generation
     *
     * @return hashmap
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("usage", usage);
        map.put("type", type);
        map.put("unit", unit);

        return map;
    }


    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }


    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getMeter_name() {
        return meter_name;
    }

    public void setMeter_name(String meter_name) {
        this.meter_name = meter_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
