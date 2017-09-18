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
 * Description: Structure for JSON mapping of Charge records
 */
public class CDR implements PersistentObject {
    public static Table TABLE = table(name("cdr"));

    public static Field<String> METRIC_FIELD = field(name("metric"), String.class);
    private String metric;

    public static Field<String> ACCOUNT_FIELD = field(name("account"), String.class);
    private String account;

    public static Field<Double> CHARGE_FIELD = field(name("charge"), Double.class);
    private Double charge = 0d;

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
    public static Field<String> CURRENCY_FIELD = field(name("currency"), String.class);
    private String currency = "N/A";

    // Sequence number coming from the Bill Run (field not persisted in DB)
    private Integer run = null;

    // empty constructor for GSON
    public CDR() {
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

    public Double getCharge() {
        return charge;
    }
    public void setCharge(Double charge) {
        this.charge = charge;
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

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    public Integer getRun() {
        return run;
    }
    public void setRun(Integer run) {
        this.run = run;
    }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(METRIC_FIELD, ACCOUNT_FIELD, CHARGE_FIELD, TIME_FROM_FIELD, TIME_TO_FIELD, DATA_FIELD, CURRENCY_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getMetric(), getAccount(), getCharge(), new Timestamp(getTime_from()),
                new Timestamp(getTime_to()), getData(), getCurrency()};
    }

    /**
     * Transform list of CDRs having data field, from PGObject to Map
     * @param CDRs list coming from the database
     * @return transformed database
     */
    public static List<CDR> applyPGObjectDataFieldToMapTransformation(List<CDR> CDRs) {
        if (CDRs == null) return null;

        CDRs.forEach(cdr -> cdr.setData(PersistentObject.PGObjectFieldToMap(cdr.getData())));

        return CDRs;
    }
}
