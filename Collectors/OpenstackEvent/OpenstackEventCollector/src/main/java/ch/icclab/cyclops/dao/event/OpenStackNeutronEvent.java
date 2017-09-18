package ch.icclab.cyclops.dao.event;
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

import ch.icclab.cyclops.dao.OpenStackEvent;
import org.jooq.Field;
import org.jooq.Table;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenStackNeutronEvent data
 */
public class OpenStackNeutronEvent extends OpenStackEvent {
    public OpenStackNeutronEvent(){
    }
    public OpenStackNeutronEvent(String account, String source, String ip_adress, String type, Long time, String region) {
        this.account = account;
        this.source = source;
        this.ip_adress = ip_adress;
        this.type = type;
        this.time = time;
        this.region = region;
    }

    private static Table TABLE = table(name("neutron_event"));

    private String ip_adress;
    public static Field<String> IP_ADRESS_FIELD = field(name("ip_address"), String.class);


    public String getIp_adress() { return ip_adress; }

    public void setIp_adress(String ip_adress) { this.ip_adress = ip_adress; }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(ACCOUNT_FIELD, SOURCE_FIELD, TYPE_FIELD, REGION_FIELD, TIME_FIELD, PROCESSED_FIELD,
                IP_ADRESS_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getAccount(), getSource(), getType(), getRegion(), new Timestamp(getTime()), isProcessed(),
                getIp_adress()};
    }
}