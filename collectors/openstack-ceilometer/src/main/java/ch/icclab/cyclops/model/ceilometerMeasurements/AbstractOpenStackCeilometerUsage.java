package ch.icclab.cyclops.model.ceilometerMeasurements;

import ch.icclab.cyclops.model.OpenStackMeter;
import ch.icclab.cyclops.model.OpenStackUsageData;
import ch.icclab.cyclops.persistence.CumulativeMeterUsage;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.util.Constant;
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

public abstract class AbstractOpenStackCeilometerUsage {

    private String _class = this.getClass().getSimpleName();

    //Account name of the user
    private String account;

    //Usage value
    private Double usage;

    //Measurement time as a timestamp
    private Long time;

    // Dashboard graph representation
    private String chartType;

    //Meter Unit
    private String unit;

    //Meta hashmap
    private HashMap<String, Object> metadata;


    public AbstractOpenStackCeilometerUsage(OpenStackUsageData usageData, OpenStackMeter meter) {
        this.set_class(this.getClass().getSimpleName());
        
//        if (meter.getType().equalsIgnoreCase(Constant.CEILOMETER_CUMULATIVE_METER))
//            usage = getCumulativeUsage(usageData);
//        else
            usage = usageData.getAvg();

        account = (String) usageData.getGroupby().get("user_id");
        unit = usageData.getUnit();
        time = getMilisForTime(usageData.getDuration_start()) / 1000;
        metadata = fillMetaData(usageData, meter);
        unit = usageData.getUnit();
    }

    public abstract AbstractOpenStackCeilometerUsage loadApplication(OpenStackUsageData udr, OpenStackMeter meter);

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

    private HashMap<String, Object> fillMetaData(OpenStackUsageData udr, OpenStackMeter meter) {
        HashMap<String, Object> meta = new HashMap<String, Object>();
        meta.put("project_id", udr.getGroupby().get("project_id"));
        meta.put("resource_id", udr.getGroupby().get("resource_id"));

        return meta;
    }

    private Double getCumulativeUsage(OpenStackUsageData usageData){
        HibernateClient hibernateClient = HibernateClient.getInstance();
        CumulativeMeterUsage usage = (CumulativeMeterUsage) hibernateClient.getObject(CumulativeMeterUsage.class, 1l);

        Double last;
        if (usage == null) {
            last = 0.0;
        } else {
            last = usageData.getAvg() - usage.getUsageCounter();
        }

        return last;
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

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
