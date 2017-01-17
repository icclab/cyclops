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
import ch.icclab.cyclops.consume.data.mapping.messages.NovaEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
import ch.icclab.cyclops.util.Time;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Created: 12-Jan-17
 * Description: This class is responsible for managing Nova messages
 */
public class NovaConsumer extends AbstractConsumer {
    @Override
    protected OpenstackNovaEvent manageMessage(String content) {
        Gson mapper = new Gson();
        //list of nova methods related to nova InstanceUpTime
        List<String> listOfActions  = Arrays.asList( settings.getOpenstackCollectorEventDelete(),
                settings.getOpenstackCollectorEventStop(), settings.getOpenstackCollectorEventSuspend(),
                settings.getOpenstackCollectorEventPause(), settings.getOpenstackCollectorEventRun());
        NovaEvent osloEvent = mapper.fromJson(content, NovaEvent.class);
        NovaEvent.OsloMessage.Args args = osloEvent.getOsloMessage().getArgs();
        String method = "";
        try{
            method = args.getKwargs().getExpected_task_state().toString();
        } catch (Exception ignored){
        }
        if (args.getObjmethod().equals("destroy"))  {
            if (args.getObjinst().getNova_objectName().equals("Instance")) {
                method = settings.getOpenstackCollectorEventDelete();
            }
        }
        String type = getType(method);
        String time = osloEvent.getOsloMessage().get_context_timestamp();
        if(listOfActions.contains(type)) {
            NovaEvent.OsloMessage.Args.ObjInst.Nova_objectData novaData = args.getObjinst().getNova_objectData();
            String instanceId = novaData.getUuid();
            String instanceName = novaData.getDisplay_name();
            String account = osloEvent.getOsloMessage().get_context_project_id();
            Double memory = novaData.getMemory_mb();
            Double vcpus = novaData.getVcpus();
            String imageDescription = novaData.getSystem_metadata().getImage_description();
            String image = novaData.getSystem_metadata().getImage_base_image_ref();
            Double disk = novaData.getEphemeral_gb() + novaData.getRoot_gb();
            return new OpenstackNovaEvent(account, instanceId, instanceName, type, memory, vcpus,
                    Time.fromNovaTimeToMills(time), image, imageDescription, disk);
        }
        return null;
    }
}
