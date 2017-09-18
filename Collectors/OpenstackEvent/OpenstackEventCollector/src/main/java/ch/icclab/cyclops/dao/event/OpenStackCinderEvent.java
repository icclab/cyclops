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
 * Author: Oleksii
 * Date: 01/04/2016
 * Description: This class holds the OpenStackCinderEvent data
 */
public class OpenStackCinderEvent extends OpenStackEvent {

    // empty constructor for GSON
    public OpenStackCinderEvent(){  }

    public OpenStackCinderEvent(String account, String source, String type,
                                Double disk, Long time, String volumeName, String instanceId, String region) {
        this.account = account;
        this.source = source;
        this.volumeName = volumeName;
        this.type = type;
        this.disk = disk;
        this.time = time;
        this.instanceId = instanceId;
        this.region = region;

    }

    public static Table TABLE = table(name("cinder_event"));

    private double disk;
    public static Field<Double> DISK_FIELD = field(name("disk"), Double.class);

    private String volumeName;
    public static Field<String> VOLUME_NAME_FIELD = field(name("volume_name"), String.class);

    private String instanceId;
    public static Field<String> INSTANCE_ID_FIELD = field(name("instance_id"), String.class);

    public double getDisk() {
        return disk;
    }

    public void setDisk(double disk) {
        this.disk = disk;
    }

    public String getVolumeName() { return volumeName; }

    public void setVolumeName(String volumeName) { this.volumeName = volumeName; }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(ACCOUNT_FIELD, SOURCE_FIELD, TYPE_FIELD, REGION_FIELD, TIME_FIELD, PROCESSED_FIELD,
                DISK_FIELD, VOLUME_NAME_FIELD, INSTANCE_ID_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getAccount(), getSource(), getType(), getRegion(), new Timestamp(getTime()), isProcessed(),
                getDisk(), getVolumeName(), getInstanceId()};
    }
}