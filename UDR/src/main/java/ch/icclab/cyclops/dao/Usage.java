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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 20.04.17
 * Description: Structure for JSON mapping of Usage records
 */
public class Usage implements PersistentObject {
    public static Table TABLE = table(name("usage"));

    public static Field<String> METRIC_FIELD = field(name("metric"), String.class);
    private String metric;

    public static Field<String> ACCOUNT_FIELD = field(name("account"), String.class);
    private String account;

    public static Field<Double> USAGE_FIELD = field(name("usage"), Double.class);
    private Double usage;

    // auto-generated if not present
    public static Field<Timestamp> TIME_FIELD = field(name("time"), Timestamp.class);
    private long time = System.currentTimeMillis();

    // unstructured data is stored here
    public static Field<String> DATA_FIELD = field(name("data"), String.class);
    private String data;

    // optional fields
    public static Field<String> UNIT_FIELD = field(name("unit"), String.class);
    private String unit = "N/A";

    // empty constructor for GSON
    public Usage() {
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

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(METRIC_FIELD, ACCOUNT_FIELD, USAGE_FIELD, TIME_FIELD, DATA_FIELD, UNIT_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getMetric(), getAccount(), getUsage(),
                new Timestamp(getTime()), getData(), getUnit()};
    }
}
