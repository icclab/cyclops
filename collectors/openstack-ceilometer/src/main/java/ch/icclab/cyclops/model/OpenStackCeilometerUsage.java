package ch.icclab.cyclops.model;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;

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

public class OpenStackCeilometerUsage {

    private String _class;

    //Usage value
    private Double usage;

    //Account name of the user
    private String account;

    //Measurement time as a timestamp
    private Long time;

    //Meter name
    private String meter_name;

    //Meta hashmap
    private HashMap<String,Object> metadata;


    public OpenStackCeilometerUsage(OpenStackUsageData udr, OpenStackMeter meter) {
        this.set_class(this.getClass().getSimpleName());

        account = (String) udr.getGroupby().get("user_id");
        usage = udr.getAvg();
        meter_name = meter.getName();
        time = getMilisForTime(udr.getDuration_start()) / 1000;
        metadata = fillMetaData(udr, meter);
    }

    /**
     * Will compute number of milliseconds from epoch to startDate
     *
     * @param time as string
     * @return milliseconds since epoch
     */
    public static long getMilisForTime(String time) {
        // first we have to get rid of 'T', as we need just T
        String isoFormat = time.replace("'", "");

        // then we have to create proper formatter
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                .withLocale(Locale.ROOT)
                .withChronology(ISOChronology.getInstanceUTC());

        // and now parse it
        DateTime dt = formatter.parseDateTime(isoFormat);

        return dt.getMillis();
    }

    private HashMap<String,Object> fillMetaData(OpenStackUsageData udr, OpenStackMeter meter){
        HashMap<String,Object> meta = new HashMap<String, Object>();
        meta.put("count", udr.getCount());
        meta.put("duration_end", udr.getDuration_end());
        meta.put("min", udr.getMin());
        meta.put("max", udr.getMax());
        meta.put("sum", udr.getSum());
        meta.put("period", udr.getPeriod());
        meta.put("period_end", udr.getPeriod_end());
        meta.put("duration", udr.getDuration());
        meta.put("period_start", udr.getPeriod_start());
        meta.put("unit", udr.getUnit());
        meta.put("project_id", udr.getGroupby().get("project_id"));
        meta.put("resource_id", udr.getGroupby().get("resource_id"));
        meta.put("type", meter.getType());

        return meta;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Double getUsage() {
        return usage;
    }

    public void setUsage(Double usage) {
        this.usage = usage;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
