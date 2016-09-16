/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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
package ch.icclab.cyclops.consume.data.mapping.openstack.events;

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.load.Loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Oleksii
 * Date: 01/04/2016
 * Description: This class holds the OpenstackNovaEvent data
 */
public class OpenstackNovaEvent extends OpenstackEvent{

    public OpenstackNovaEvent(String account, String instanceId, String type, Double memory, Double vcpus, String time){
        this.account = account;
        this.instanceId = instanceId;
        this.type = type;
        this.memory = memory;
        this.vcpus = vcpus;
        this.time = time;
    }

    private Double memory;

    private Double vcpus;

    public void setMemory(Double memory) {this.memory = memory;}

    public void setVcpus(Double vcpus) {this.vcpus = vcpus;}

    public Double getMemory() { return memory; }

    public Double getVcpus() { return vcpus; }

    /**
     * @return table
     */
    public  String getTableName() {
        return Loader.getSettings().getOpenstackSettings().getOpenstackEventNovaTable();
    }

    /**
     * Get fields for point generation
     *
     * @return hashmap
     */
    public Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("instanceId", instanceId);
        map.put("account", account);
        map.put("type", type);
        map.put("memory", memory.toString());
        map.put("cpu", vcpus.toString());

        return map;
    }
    public String getDateFormat(){
        return "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
    }


}
