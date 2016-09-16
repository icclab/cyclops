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

package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.data.mapping.messages.NeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.messages.OsloEvent;
import ch.icclab.cyclops.consume.data.mapping.messages.OsloEvent.OsloMessage.Args;
import ch.icclab.cyclops.consume.data.mapping.messages.OsloEvent.OsloMessage.Args.ObjInst.Nova_objectData;
import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;


/**
 * Author: Skoviera
 * Created: 14/04/16
 * Updated: Oleksii 01/06/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    private static InfluxDBClient influxDBClient = InfluxDBClient.getInstance();
    private static OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();

    @Override
    protected void consume(String content) {
        OpenstackEvent data =null;
        try {
            data = manageNovaMessage(content);
        } catch (Exception ignored) {
        }
        try{
            data = manageNeuntronMessage(content);
        }catch (Exception ignored) {
        }

        if (data != null) influxDBClient.persistSinglePoint(data.getPoint());
    }

    private OpenstackNovaEvent manageNovaMessage(String content) {
        Gson mapper = new Gson();
        //list of nova methods related to nova InstanceUpTime
        List<String> listOfActions  = Arrays.asList( settings.getOpenstackCollectorEventDelete(),
                settings.getOpenstackCollectorEventResize(), settings.getOpenstackCollectorEventStop(),
                settings.getOpenstackCollectorEventSuspend(), settings.getOpenstackCollectorEventPause(),
                settings.getOpenstackCollectorEventRun());
        OsloEvent osloEvent = mapper.fromJson(content, OsloEvent.class);
        Args args = osloEvent.getOsloMessage().getArgs();
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
            Nova_objectData novaData = args.getObjinst().getNova_objectData();
            String instanceId = novaData.getUuid();
            String account = osloEvent.getOsloMessage().get_context_project_name();
            Double memory = novaData.getMemory_mb();
            Double vcpus = novaData.getVcpus();
            return new OpenstackNovaEvent(account,instanceId, type, memory, vcpus, time);
        }
        return null;
    }

    private OpenstackNeutronEvent manageNeuntronMessage(String content) {
        Gson mapper = new Gson();
        List<String> listOfActions  = Arrays.asList( settings.getOpenstackCollectorEventDelete(), settings.getOpenstackCollectorEventRun());
        NeutronEvent neutronEvent = mapper.fromJson(content, NeutronEvent.class);
        String id;
        if (neutronEvent.getPayload().getFloatingip_id() != null) {
            id = neutronEvent.getPayload().getFloatingip_id();
        }  else {
            id = neutronEvent.getPayload().getFloatingip().getId();
        }
        String type = getType(neutronEvent.getEvent_type());
        if (listOfActions.contains(type)){
            return new OpenstackNeutronEvent(neutronEvent.get_context_tenant_name(), id, type, neutronEvent.get_context_timestamp());
        }

        return null;
    }

    private String getType(String method){
        List<String> listOfRunningActions = Arrays.asList("spawning", "powering-on", "unpausing", "resuming", "floatingip.create.end");
        String paused = "pausing";
        String poweredOff="[powering-off]";
        String suspend = "suspending";
        String deleted = "floatingip.delete.end";

        if (listOfRunningActions.contains(method)){
            return settings.getOpenstackCollectorEventRun();
        }
        if (method.equals(paused)){
            return settings.getOpenstackCollectorEventPause();
        }
        if (method.equals(poweredOff)){
            return settings.getOpenstackCollectorEventStop();
        }
        if (method.equals(suspend)){
            return settings.getOpenstackCollectorEventSuspend();
        }
        if (method.equals(deleted)){
            return settings.getOpenstackCollectorEventDelete();
        }
        return method;
    }
}
