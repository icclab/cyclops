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
import ch.icclab.cyclops.consume.data.mapping.messages.NovaEvent;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackNovaEvent;
import ch.icclab.cyclops.util.Time;
import com.google.gson.Gson;
import org.jooq.SelectQuery;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Created: 12-Jan-17
 * Description: This class is responsible for managing Nova messages
 */
public class NovaProcess extends EventProcess {

    public NovaProcess(String content) {
        super(content);
    }

    public NovaProcess(String content, ConsumerEntry consumer, Long deliveryTag, boolean healthCheck) {
        super(content, consumer, deliveryTag, healthCheck);
    }

    @Override
    protected List<OpenStackEvent> manageMessage(String content) {
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
        } catch (Exception ignored){} try {
            if (args.getObjmethod().equals("destroy"))  {
                if (args.getObjinst().getNova_objectName().equals("Instance")) {
                    method = settings.getOpenstackCollectorEventDelete();
                }
            }
        } catch (Exception ignored){}try {
            if (args.getObjinst().getNova_objectData().getVm_state().equals("deleted"))  {
                method = settings.getOpenstackCollectorEventDelete();
            }
        } catch (Exception ignored){}
        String type = getType(method);
        String time = osloEvent.getOsloMessage().get_context_timestamp();
        if(listOfActions.contains(type)) {
            NovaEvent.OsloMessage.Args.ObjInst.Nova_objectData novaData = args.getObjinst().getNova_objectData();
            String instanceId = novaData.getUuid();
            String instanceName = novaData.getDisplay_name();
            String account;
            try {
                account = osloEvent.getOsloMessage().get_context_project_id();
            } catch (Exception ignored){
                account = "";
            }

            double memory = novaData.getMemory_mb();
            double vcpus = novaData.getVcpus();
            String flavor;
            try {
                flavor = novaData.getFlavor().getNova_objectData().getName();
            } catch (Exception ignored){
                flavor = novaData.getSystem_metadata().getInstance_type_name();
            }
            double disk = novaData.getRoot_gb();
            double ephemeral = novaData.getEphemeral_gb();
            long mills = Time.fromNovaTimeToMills(time);
            String region = settings.getOpenstackDefaultRegion();
            Integer attachment = settings.getDefaultVolumeAttachment();
            OpenStackNovaEvent novaEvent = getLastNovaEvent(instanceId);
            if (novaEvent != null) {
                attachment = novaEvent.getNumberVolumes();
            }
            return Arrays.asList(new OpenStackNovaEvent(account, instanceId, instanceName, type, memory, vcpus,
                    mills, disk, ephemeral, attachment, flavor, region));
        }
        return null;
    }
}
