package ch.icclab.cyclops.dao;
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

import org.jooq.Field;
import org.jooq.Table;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.*;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 20.04.17
 * Description: Structure for JSON mapping of Usage records
 */
public class UDR implements PersistentObject {
    public static Table TABLE = table(name("udr"));

    public static Field<String> METRIC_FIELD = field(name("metric"), String.class);
    private String metric;

    public static Field<String> ACCOUNT_FIELD = field(name("account"), String.class);
    private String account;

    public static Field<Double> USAGE_FIELD = field(name("usage"), Double.class);
    private Double usage = 0d;

    // auto-generated if not present
    public static Field<Timestamp> TIME_FROM_FIELD = field(name("time_from"), Timestamp.class);
    private long time_from = System.currentTimeMillis();

    public static Field<Timestamp> TIME_TO_FIELD = field(name("time_to"), Timestamp.class);
    private long time_to = System.currentTimeMillis();

    // unstructured data is stored here
    public static Field<String> DATA_FIELD = field(name("data"), String.class);
    // NOTE: when persisting it needs to be String, when querying it will come back as PGObject
    private Object data;

    // optional fields
    public static Field<String> UNIT_FIELD = field(name("unit"), String.class);
    private String unit;

    // empty constructor for GSON
    public UDR() {
    }

    //=========== Getters and Setters
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

    public Double getUsage() {
        return usage;
    }
    public void setUsage(Double usage) {
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

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(METRIC_FIELD, ACCOUNT_FIELD, USAGE_FIELD, TIME_FROM_FIELD, TIME_TO_FIELD, DATA_FIELD, UNIT_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getMetric(), getAccount(), getUsage(), new Timestamp(getTime_from()),
                new Timestamp(getTime_to()), getData(), getUnit()};
    }

    /**
     * Transform list of UDRs having data field, from PGObject to Map
     * @param UDRs list coming from the database
     * @return transformed data field
     */
    public static List<UDR> applyPGObjectDataFieldToMapTransformation(List<UDR> UDRs) {
        if (UDRs == null) return null;

        UDRs.forEach(udr -> udr.setData(PersistentObject.PGObjectFieldToMap(udr.getData())));

        return UDRs;
    }
}
