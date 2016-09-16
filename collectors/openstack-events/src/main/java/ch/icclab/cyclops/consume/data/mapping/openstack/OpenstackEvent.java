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
package ch.icclab.cyclops.consume.data.mapping.openstack;

import org.influxdb.dto.Point;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This abstract class holds the OpenstackEvent data
 */
public abstract class OpenstackEvent {

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isValid() {
        return type != null;
    }

    public String account;
    public String instanceId;
    public String type;
    public String time;

    /**
     * This public method will access data and create db Point
     *
     * @return db point
     */
    public Point getPoint() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(getDateFormat()).withZoneUTC();
        DateTime dt = formatter.parseDateTime(time);
        Map fields = getFields();
        removeNullValues(fields);

        return Point.measurement(getTableName())
                .time(dt.getMillis(), MILLISECONDS)
                .fields(fields)
                .build();
    }

    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    protected abstract String getTableName();

    protected abstract  Map<String, Object> getFields();

    protected abstract String getDateFormat();

}
