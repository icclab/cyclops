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


/**
 * Author: Oleksii
 * Date: 01/04/2016
 * Description: This class holds the OpenstackCinderEvent data
 */
public class OpenstackCinderEvent extends OpenstackEvent {

    public OpenstackCinderEvent(){

    }

    public OpenstackCinderEvent(String account, String source, String type, Double disk, Long time, String instanceId) {
        this.account = account;
        this.source = source;
        this.type = type;
        this.disk = disk;
        this.time = time;
        this.instanceId = instanceId;
    }
    private Double disk;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Double getDisk() {
        return disk;
    }

    public void setDisk(Double disk) {
        this.disk = disk;
    }

    private String instanceId;
}