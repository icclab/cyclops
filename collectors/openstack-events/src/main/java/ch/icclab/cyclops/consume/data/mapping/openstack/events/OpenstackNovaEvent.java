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

    public OpenstackNovaEvent(){

    }

    public OpenstackNovaEvent(String account, String resourceId, String type, Double memory, Double vcpus, Long time, String image, Double disk){
        this.account = account;
        this.resourceId = resourceId;
        this.type = type;
        this.memory = memory;
        this.vcpus = vcpus;
        this.time = time;
        this.image = image;
        this.disk = disk;
    }

    private Double memory;

    private Double vcpus;

    private String image;

    private Double disk;

    private Boolean valueAttached = false;

    public void attachValue(){
        this.valueAttached = true;
    }

    public void deattachValue(){
        this.valueAttached = false;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Double getVcpus() {
        return vcpus;
    }

    public void setVcpus(Double vcpus) {
        this.vcpus = vcpus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getDisk() {
        return disk;
    }

    public void setDisk(Double disk) {
        this.disk = disk;
    }

    public Boolean getValueAttached() {
        return valueAttached;
    }

    public void setValueAttached(Boolean valueAttached) {
        this.valueAttached = valueAttached;
    }
}
