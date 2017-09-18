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

import ch.icclab.cyclops.consume.command.generation.usage.OpenStackUpTimeUsage;
import ch.icclab.cyclops.consume.command.generation.usage.Usage;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackNovaEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Nova usage records out of events and send to the queue
 */
public  class NovaUsageGeneration extends OpenStackAbstractGeneration {

    @Override
    public OpenStackEvent getEventFormat(){ return new OpenStackNovaEvent(); }

    @Override
    public Class getUsageFormat(){ return OpenStackUpTimeUsage.class; }

    @Override
    public ArrayList<Usage> generateValue(long eventTime, OpenStackEvent lastEventInScope){
        OpenStackNovaEvent transformedEvent = (OpenStackNovaEvent) lastEventInScope;
        String type  = transformedEvent.getType();
        String _classCPU = null;
        String _classMemory = null;
        String _classRoot = null;
        String _classEphemeral = null;
        OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();

        if (type.equals(settings.getOpenstackCollectorEventRun())){
            _classCPU = "OpenStackCPUActiveUsage";
            _classMemory = "OpenStackMemoryActiveUsage";
            _classRoot = "OpenStackRootActiveUsage";
            _classEphemeral = "OpenStackEphemeralActiveUsage";
        }
        if (type.equals(settings.getOpenstackCollectorEventStop())){
            _classCPU = "OpenStackCPUStoppedUsage";
            _classMemory = "OpenStackMemoryStoppedUsage";
            _classRoot = "OpenStackRootStoppedUsage";
            _classEphemeral = "OpenStackEphemeralStoppedUsage";
        }
        if (type.equals(settings.getOpenstackCollectorEventPause())){
            _classCPU = "OpenStackCPUPausedUsage";
            _classMemory = "OpenStackMemoryPausedUsage";
            _classRoot = "OpenStackRootPausedUsage";
            _classEphemeral = "OpenStackEphemeralPausedUsage";
        }
        if (type.equals(settings.getOpenstackCollectorEventSuspend())){
            _classCPU = "OpenStackCPUSuspendedUsage";
            _classMemory = "OpenStackMemorySuspendedUsage";
            _classRoot = "OpenStackRootSuspendedUsage";
            _classEphemeral = "OpenStackEphemeralSuspendedUsage";
        }

        ArrayList<Usage> generatedUsages = new ArrayList<>();
        HashMap<String, Double> meters = new HashMap<>();
        meters.put(_classCPU, transformedEvent.getVcpus());
        meters.put(_classMemory, transformedEvent.getMemory());
        meters.put(_classRoot, transformedEvent.getDisk());
        meters.put(_classEphemeral, transformedEvent.getEphemeral());
        long eventLastTime = transformedEvent.getTime();
        long currentTime;
        String account = transformedEvent.getAccount();
        String sourceId = transformedEvent.getSource();
        String flavor = transformedEvent.getFlavor();

        if (second_step !=0){
            Boolean lastIteration = false;
            do {
                currentTime = eventLastTime + second_step;
                if (currentTime >= eventTime){
                    currentTime = eventTime;
                    lastIteration = true;
                }

                for (String meter: meters.keySet()){
                        generatedUsages.add(new OpenStackUpTimeUsage(
                                currentTime,
                                account,
                                sourceId,
                                transformedEvent.getSource_name(),
                                (double) (currentTime - eventLastTime)/1000,
                                meters.get(meter),
                                meter, flavor, transformedEvent.getNumberVolumes(),
                                transformedEvent.getRegion()));
                }

                eventLastTime = currentTime;
            } while (!lastIteration);

            return generatedUsages;
        } else {
            for (String meter: meters.keySet()){
                generatedUsages.add(new OpenStackUpTimeUsage(
                        eventTime,
                        account,
                        sourceId,
                        transformedEvent.getSource_name(),
                        (double) (eventTime - eventLastTime)/1000,
                        meters.get(meter),
                        meter, flavor, transformedEvent.getNumberVolumes(),
                        transformedEvent.getRegion()));
            }
            return generatedUsages;
        }
    }
}

