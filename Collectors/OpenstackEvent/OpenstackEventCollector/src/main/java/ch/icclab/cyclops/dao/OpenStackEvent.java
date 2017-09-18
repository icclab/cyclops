package ch.icclab.cyclops.dao;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import org.jooq.Field;

import java.sql.Timestamp;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;


/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This abstract class holds the OpenStackEvent data
 */
public abstract class OpenStackEvent implements PersistentObject {

    public static Field<String> ACCOUNT_FIELD = field(name("account"), String.class);
    protected String account;

    public static Field<String> SOURCE_FIELD = field(name("source"), String.class);
    protected String source;

    public static Field<String> TYPE_FIELD = field(name("type"), String.class);
    protected String type;

    public static Field<String> REGION_FIELD = field(name("region"), String.class);
    protected String region;

    public static Field<Boolean> PROCESSED_FIELD = field(name("processed"), Boolean.class);
    private boolean processed = false;

    // auto-generated if not present
    public static Field<Timestamp> TIME_FIELD = field(name("time"), Timestamp.class);
    protected long time = System.currentTimeMillis();

    //=========== Getters and Setters
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) { this.time = time; }

    public String getRegion() { return region; }

    public void setRegion(String region) { this.region = region; }

    public boolean isProcessed() { return processed;  }

    public void setProcessed(boolean processed) { this.processed = processed; }

}
