package ch.icclab.cyclops.schedule.runner.openstack;

/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackCinderEvent;
import ch.icclab.cyclops.consume.data.mapping.usage.OpenStackVolumeActiveUsage;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.runner.OpenStackClient;
import java.util.ArrayList;


/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Cinder usage records out of events and send to the queue
 */
public class CinderUDRRunner extends OpenStackClient {

    @Override
    public String getDbName() {
        return OpenstackCinderEvent.class.getSimpleName();
    }

    @Override
    public ArrayList<Class> getListOfMeasurements(){
        return new ArrayList<Class>() {{
            add(OpenStackVolumeActiveUsage.class);
        }};
    }

    @Override
    public Class getUsageFormat(){
        return OpenstackCinderEvent.class;
    }

    @Override
    public ArrayList<OpenStackVolumeActiveUsage> generateValue(Long eventTime, OpenstackEvent lastEventInScope) {
        OpenstackCinderEvent transformedEvent = (OpenstackCinderEvent) lastEventInScope;
        ArrayList<OpenStackVolumeActiveUsage> generatedUsages = new ArrayList<>();
        Long eventLastTime = transformedEvent.getTime();
        Long scheduleTime = new Long(Loader.getSettings().getOpenstackSettings().getOpenstackScheduleTime());
        Long currentTime;

        Boolean lastIteration = false;
        do {
            currentTime = eventLastTime + scheduleTime;
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
                    transformedEvent.getDisk()));
            eventLastTime = currentTime;
        } while (!lastIteration);
        return generatedUsages;
    }

}