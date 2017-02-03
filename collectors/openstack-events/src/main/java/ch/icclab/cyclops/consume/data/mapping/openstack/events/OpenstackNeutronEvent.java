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
import ch.icclab.cyclops.timeseries.GenerateDBPoint;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenstackNeutronEvent data
 */
public class OpenstackNeutronEvent extends OpenstackEvent {
    public OpenstackNeutronEvent(){
    }
    public OpenstackNeutronEvent(String account, String source, String ip_adress, String type, Long time) {
        this.account = account;
        this.source = source;
        this.ip_adress = ip_adress;
        this.type = type;
        this.time = time;
    }

    private String ip_adress;

    public String getIp_adress() { return ip_adress; }

    public void setIp_adress(String ip_adress) { this.ip_adress = ip_adress; }
}