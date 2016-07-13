package ch.icclab.cyclops.model;

import java.util.Collections;
import java.util.Map;

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
 * Created by Manu Perez on 30/05/16.
 */

public class OpenStackUsageData {
    private String _class;
    private static String PREFIX = "OpenStack";

    //Data output count
    private Integer count;

    //Date where the measurement started
    private String duration_start;

    //Date where the measurement ended
    private String duration_end;

    //Minimum value in the measurement
    private Double min;

    //Maximum value in the measurement
    private Double max;

    //Sum of values in the measurement
    private Double sum;

    //Average value in the measurement
    private Double avg;

    //Collection period
    private Double period;

    //Date where the collection period started
    private String period_start;

    //Date where the collection period ended
    private String period_end;

    //Duration of the measurement
    private Double duration;

    //Measurement usage unit
    private String unit;

    //Hash map containing project_id, user_id and resource_id
    private Map groupby;


    public OpenStackUsageData() {
        setClassName();
    }

    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    protected void setClassName() {
        this.set_class(String.format("%s%s", OpenStackUsageData.PREFIX, this.getClass().getSimpleName()));
    }
    //Getters and Setters


    public static String getPREFIX() {
        return PREFIX;
    }

    public static void setPREFIX(String PREFIX) {
        OpenStackUsageData.PREFIX = PREFIX;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDuration_start() {
        return duration_start;
    }

    public void setDuration_start(String duration_start) {
        this.duration_start = duration_start;
    }

    public String getDuration_end() {
        return duration_end;
    }

    public void setDuration_end(String duration_end) {
        this.duration_end = duration_end;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Double getPeriod() {
        return period;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }

    public String getPeriod_start() {
        return period_start;
    }

    public void setPeriod_start(String period_start) {
        this.period_start = period_start;
    }

    public String getPeriod_end() {
        return period_end;
    }

    public void setPeriod_end(String period_end) {
        this.period_end = period_end;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Map getGroupby() {
        return groupby;
    }

    public void setGroupby(Map groupby) {
        this.groupby = groupby;
    }
}
