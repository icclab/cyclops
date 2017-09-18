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
 * Description: Structure for JSON mapping of Bill runs
 */
public class BillRun implements PersistentObject {
    public static Table TABLE = table(name("billrun"));

    public static Field<Integer> ID_FIELD = field(name("id"), Integer.class);
    private Integer id;

    public static Field<Timestamp> TIME_FIELD = field(name("time"), Timestamp.class);
    private long time = System.currentTimeMillis();

    // unstructured data is stored here
    public static Field<String> DATA_FIELD = field(name("data"), String.class);
    // NOTE: when persisting it needs to be String, when querying it will come back as PGObject
    private Object data;

    // empty constructor for GSON
    public BillRun() {
    }

    //=========== Getters and Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
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

    /**
     * We do not include ID field, as it's SERIAL type and we don't want to set it ourselves
     * @return collection of fields
     */
    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(TIME_FIELD, DATA_FIELD);
    }

    /**
     * We do not include ID value, as it's SERIAL type and we don't want to set it ourselves
     * @return an array of values
     */
    @Override
    public Object[] getValues() {
        return new Object[]{new Timestamp(getTime()), getData()};
    }

    /**
     * Transform list of Bill runs having data field, from PGObject to Map
     * @param runs list coming from the database
     * @return transformed database
     */
    public static List<BillRun> applyPGObjectDataFieldToMapTransformation(List<BillRun> runs) {
        if (runs == null) return null;

        runs.forEach(run -> run.setData(PersistentObject.PGObjectFieldToMap(run.getData())));

        return runs;
    }
}
