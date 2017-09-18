package ch.icclab.cyclops.consume.data.process;
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

import ch.icclab.cyclops.consume.ConsumerEntry;
import ch.icclab.cyclops.consume.data.EventProcess;
import ch.icclab.cyclops.consume.data.mapping.messages.CinderEvent;
import ch.icclab.cyclops.dao.event.OpenStackCinderEvent;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackNovaEvent;
import ch.icclab.cyclops.util.Time;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Created: 12-Jan-17
 * Description: This class is responsible for managing Cinder messages
 */
public class CinderProcess extends EventProcess {
    public CinderProcess(String content) {
        super(content);
    }

    public CinderProcess(String content, ConsumerEntry consumer, Long deliveryTag, boolean healthCheck) {
        super(content, consumer, deliveryTag, healthCheck);
    }

    @Override
    protected List<OpenStackEvent> manageMessage(String content) {
        Gson mapper = new Gson();
        List<String> listOfActions = Arrays.asList(settings.getOpenstackCollectorEventDelete(), settings.getOpenstackCollectorEventRun());
        CinderEvent.OsloMessage cinderEvent = mapper.fromJson(content, CinderEvent.class).getOsloMessage();
        String volumeId = cinderEvent.getPayload().getVolume_id();
        String type = getType(cinderEvent.getEvent_type());
        if (listOfActions.contains(type)) {
            String tenant = cinderEvent.get_context_tenant();
            double size = cinderEvent.getPayload().getSize();
            String contextTime = cinderEvent.get_timestamp();
            long timestamp = Time.fromOpenstackTimeToMills(contextTime);
            String volumeName = cinderEvent.getPayload().getDisplay_name();
            String region = settings.getOpenstackDefaultRegion();
            String instanceId = null;
            OpenStackCinderEvent cinderLastEvent = getLastCinderEvent(volumeId);
            try {
                instanceId = cinderLastEvent.getInstanceId();
            } catch (Exception ignored){
            }

            return Arrays.asList(new OpenStackCinderEvent(tenant, volumeId, type, size, timestamp, volumeName, instanceId, region));
        }

        // change attach status in nova table
        if (cinderEvent.getEvent_type().equals("volume.attach.end")) {
            return updateVolumeStatus(cinderEvent, true);
        }

        if (cinderEvent.getEvent_type().equals("volume.detach.end")) {
            return updateVolumeStatus(cinderEvent, false);
        }
        return null;
    }



    /**
     * Update attach status in nova event table
     */

    private List<OpenStackEvent> updateVolumeStatus(CinderEvent.OsloMessage cinderData, Boolean valueStatus) {
        try {
            List<OpenStackEvent> events = new ArrayList();
            String source;
            String volumeId = cinderData.getPayload().getVolume_id();
            long time = Time.fromOpenstackTimeToMills(cinderData.get_timestamp());

            OpenStackCinderEvent cinderEvent = getLastCinderEvent(volumeId);
            if (cinderEvent != null) {
                cinderEvent.setTime(time);
            }

            if (valueStatus) {
                source = cinderData.getPayload().getVolume_attachment().get(0).getInstance_uuid();
                cinderEvent.setInstanceId(source);
            } else {
                source = cinderEvent.getInstanceId();
                cinderEvent.setInstanceId(null);
            }

            events.add(cinderEvent);
            OpenStackNovaEvent novaEvent = getLastNovaEvent(source);
            if (novaEvent != null) {
                int number = novaEvent.getNumberVolumes();
                novaEvent.setTime(time);
                if (valueStatus) {
                    novaEvent.setNumberVolumes(number + 1);
                } else {
                    novaEvent.setNumberVolumes(number - 1);
                }
                events.add(novaEvent);
            }
            return events;

        } catch (Exception ignored) {
            return null;
        }

    }
}
