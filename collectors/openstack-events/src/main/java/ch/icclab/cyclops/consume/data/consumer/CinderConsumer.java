package ch.icclab.cyclops.consume.data.consumer;
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

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.data.mapping.messages.CinderEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackCinderEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
import ch.icclab.cyclops.util.Time;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Created: 12-Jan-17
 * Description: This class is responsible for managing Cinder messages
 */
public class CinderConsumer extends AbstractConsumer {
    @Override
    protected OpenstackCinderEvent manageMessage(String content) {
        Gson mapper = new Gson();
        List<String> listOfActions  = Arrays.asList( settings.getOpenstackCollectorEventDelete(), settings.getOpenstackCollectorEventRun());
        CinderEvent.OsloMessage cinderEvent = mapper.fromJson(content, CinderEvent.class).getOsloMessage();
        String id = cinderEvent.getPayload().getVolume_id();

        String type = getType(cinderEvent.getEvent_type());
        if (listOfActions.contains(type)){
            String tenant = cinderEvent.get_context_tenant();
            Double size = cinderEvent.getPayload().getSize();
            String contextTime = cinderEvent.get_timestamp();
            Long timestamp = Time.fromOpenstackTimeToMills(contextTime);
            String volumeName = cinderEvent.getPayload().getDisplay_name();
            return new OpenstackCinderEvent(tenant, id, type, size, timestamp, volumeName);
        }

        return null;
    }
}
