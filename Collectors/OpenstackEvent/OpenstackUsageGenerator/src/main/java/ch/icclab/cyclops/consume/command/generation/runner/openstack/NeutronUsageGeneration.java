package ch.icclab.cyclops.consume.command.generation.runner.openstack;
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

import ch.icclab.cyclops.consume.command.generation.usage.OpenStackFloatingIpActiveUsage;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackNeutronEvent;

import java.util.ArrayList;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Neutron usage records out of events and send to the queue
 */
public class NeutronUsageGeneration extends OpenStackAbstractGeneration {

    @Override
    public Class getUsageFormat(){ return OpenStackFloatingIpActiveUsage.class; }

    @Override
    public OpenStackEvent getEventFormat(){
        return new OpenStackNeutronEvent();
    }

    @Override
    public ArrayList<OpenStackFloatingIpActiveUsage> generateValue(long eventTime, OpenStackEvent lastEventInScope) {
        OpenStackNeutronEvent transformedEvent = (OpenStackNeutronEvent) lastEventInScope;
        ArrayList<OpenStackFloatingIpActiveUsage> generatedUsages = new ArrayList<>();
        long eventLastTime = transformedEvent.getTime();
        long currentTime;
        if (second_step != 0){
            Boolean lastIteration = false;
            do {
                currentTime = eventLastTime + second_step;
                if (currentTime >= eventTime){
                    currentTime = eventTime;
                    lastIteration = true;
                }
                generatedUsages.add( new OpenStackFloatingIpActiveUsage(
                        currentTime,
                        transformedEvent.getAccount(),
                        transformedEvent.getIp_adress(),
                        transformedEvent.getSource() ,
                        (double) (currentTime - eventLastTime)/1000,
                        transformedEvent.getRegion())
                );
                eventLastTime = currentTime;
            } while (!lastIteration);
            return generatedUsages;
        } else {
            generatedUsages.add( new OpenStackFloatingIpActiveUsage(
                    eventTime,
                    transformedEvent.getAccount(),
                    transformedEvent.getIp_adress(),
                    transformedEvent.getSource() ,
                    (double) (eventTime - eventLastTime)/1000,
                    transformedEvent.getRegion())
            );
            return generatedUsages;
        }
    }

}