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
 * Description: This class holds the OpenStackNovaEvent data
 */
public class OpenStackNovaEvent extends OpenStackEvent {

    // empty constructor for GSON
    public OpenStackNovaEvent(){ }

    public OpenStackNovaEvent(String account, String source, String source_name, String type, Double memory,
                              Double vcpus, Long time, Double disk, Double ephemeral, Integer attachment, String flavor,
                              String region){
        this.account = account;
        this.source = source;
        this.source_name = source_name;
        this.type = type;
        this.memory = memory;
        this.vcpus = vcpus;
        this.time = time;
        this.disk = disk;
        this.number_volumes = attachment;
        this.flavor = flavor;
        this.ephemeral = ephemeral;
        this.region = region;

    }

    public static Table TABLE = table(name("nova_event"));

    public static Field<Double> MEMORY_FIELD = field(name("memory"), Double.class);
    private double memory;

    public static Field<Double> VCPUS_FIELD = field(name("vcpus"), Double.class);
    private double vcpus;

    public static Field<String> SOURCE_NAME_FIELD = field(name("source_name"), String.class);
    private String source_name;

    public static Field<Double> DISK_FIELD = field(name("disk"), Double.class);
    private double disk;

    public static Field<Double> EPHEMERAL_FIELD = field(name("ephemeral"), Double.class);
    private double ephemeral;

    public static Field<String> FLAVOR_FIELD = field(name("flavor"), String.class);
    private String flavor;

    public static Field<Integer> NUMBER_VOLUMES_FIELD = field(name("number_volumes"), Integer.class);
    private int number_volumes;

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public String getSource_name() { return source_name; }

    public void setSource_name(String source_name) { this.source_name = source_name; }

    public double getVcpus() { return vcpus; }

    public void setVcpus(double vcpus) {
        this.vcpus = vcpus;
    }

    public String getFlavor() { return flavor; }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public double getDisk() {
        return disk;
    }

    public void setDisk(double disk) {
        this.disk = disk;
    }

    public double getEphemeral() { return ephemeral; }

    public void setEphemeral(double ephemeral) { this.ephemeral = ephemeral; }

    public int getNumberVolumes() {
        return number_volumes;
    }

    public void setNumberVolumes(int number_volumes) {
        this.number_volumes = number_volumes;
    }

    //=========== PersistentObject interface implementation
    @Override
    public Table<?> getTable() {
        return TABLE;
    }

    @Override
    public Collection<? extends Field<?>> getFields() {
        return Arrays.asList(ACCOUNT_FIELD, SOURCE_FIELD, TYPE_FIELD, REGION_FIELD, TIME_FIELD, PROCESSED_FIELD,
                MEMORY_FIELD, VCPUS_FIELD, SOURCE_NAME_FIELD, DISK_FIELD, EPHEMERAL_FIELD, FLAVOR_FIELD, NUMBER_VOLUMES_FIELD);
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getAccount(), getSource(), getType(), getRegion(), new Timestamp(getTime()), isProcessed(),
                getMemory(), getVcpus(),getSource_name(), getDisk(), getEphemeral(), getFlavor(), getNumberVolumes()};
    }
}
