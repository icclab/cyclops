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

import ch.icclab.cyclops.consume.command.generation.usage.OpenStackVolumeActiveUsage;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackCinderEvent;

import java.util.ArrayList;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Cinder usage records out of events and send to the queue
 */
public class CinderUsageGeneration extends OpenStackAbstractGeneration {

    @Override
    public Class getUsageFormat(){ return OpenStackVolumeActiveUsage.class; }

    @Override
    public OpenStackEvent getEventFormat(){ return new OpenStackCinderEvent(); }

    @Override
    public ArrayList<OpenStackVolumeActiveUsage> generateValue(long eventTime, OpenStackEvent lastEventInScope) {
        OpenStackCinderEvent transformedEvent = (OpenStackCinderEvent) lastEventInScope;
        ArrayList<OpenStackVolumeActiveUsage> generatedUsages = new ArrayList<>();
        long eventLastTime = transformedEvent.getTime();
        long currentTime;

        if (second_step !=0){
            Boolean lastIteration = false;
            do {
                currentTime = eventLastTime + second_step;
                if (currentTime >= eventTime){
                    currentTime = eventTime;
                    lastIteration = true;
                }
                generatedUsages.add( new OpenStackVolumeActiveUsage(
                        currentTime,
                        transformedEvent.getAccount(),
                        transformedEvent.getVolumeName(),
                        transformedEvent.getSource(),
                        (double) (currentTime - eventLastTime)/1000,
                        transformedEvent.getDisk(),
                        transformedEvent.getRegion()));
                eventLastTime = currentTime;
            } while (!lastIteration);
            return generatedUsages;
        } else {
            generatedUsages.add( new OpenStackVolumeActiveUsage(
                    eventTime,
                    transformedEvent.getAccount(),
                    transformedEvent.getVolumeName(),
                    transformedEvent.getSource(),
                    (double) (eventTime - eventLastTime)/1000,
                    transformedEvent.getDisk(),
                    transformedEvent.getRegion()));
            return generatedUsages;
        }
    }

}