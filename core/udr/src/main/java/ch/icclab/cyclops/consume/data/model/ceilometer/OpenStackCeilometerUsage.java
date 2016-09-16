package ch.icclab.cyclops.consume.data.model.ceilometer;

import ch.icclab.cyclops.consume.data.DataMapping;

import java.util.ArrayList;
import java.util.List;
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

public class OpenStackCeilometerUsage implements DataMapping{
    private String _class = this.getClass().getSimpleName();
    private long time;
    private Double usage;
    private String account;
    private String measurementId;
    private String unit;
    private Map metadata;

    @Override
    public String getTimeField() {
        return "time";
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public List<String> getTagNames() {
        List<String> list = new ArrayList<>();
        list.add("account");
        list.add("measurementId");
        return list;
    }

    @Override
    public Map preProcess(Map original) {
        return original;
    }

    @Override
    public Boolean shouldPublish() {
        return false;
    }

    @Override
    public Boolean doNotBroadcastButRoute() {
        return false;
    }

    public Double getUsage() {
        return usage;
    }

    public void setUsage(Double usage) {
        this.usage = usage;
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

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }
}
