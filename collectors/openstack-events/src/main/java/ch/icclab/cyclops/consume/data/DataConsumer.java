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
import ch.icclab.cyclops.consume.data.mapping.messages.CinderEvent;
import ch.icclab.cyclops.consume.data.mapping.messages.NeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.messages.NovaEvent;
import ch.icclab.cyclops.consume.data.mapping.messages.NovaEvent.OsloMessage.Args;
import ch.icclab.cyclops.consume.data.mapping.messages.NovaEvent.OsloMessage.Args.ObjInst.Nova_objectData;
import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackCinderEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.Time;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import com.google.gson.Gson;

import java.util.*;


/**
 * Author: Skoviera
 * Created: 14/04/16
 * Updated: Oleksii 01/06/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    private static InfluxDBClient influxDBClient = new InfluxDBClient();
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

        try{
            data = manageCinderMessage(content);
        }catch (Exception ignored) {
        }

        if (data != null) influxDBClient.persistSinglePoint(data.getPoint());
    }

    private OpenstackNovaEvent manageNovaMessage(String content) {
        Gson mapper = new Gson();
        //list of nova methods related to nova InstanceUpTime
        List<String> listOfActions  = Arrays.asList( settings.getOpenstackCollectorEventDelete(),
                settings.getOpenstackCollectorEventStop(), settings.getOpenstackCollectorEventSuspend(),
                settings.getOpenstackCollectorEventPause(), settings.getOpenstackCollectorEventRun());
        NovaEvent osloEvent = mapper.fromJson(content, NovaEvent.class);
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
            String account = osloEvent.getOsloMessage().get_context_project_id();
            Double memory = novaData.getMemory_mb();
            Double vcpus = novaData.getVcpus();
            String image = novaData.getSystem_metadata().getImage_description();
            Double disk = novaData.getEphemeral_gb() + novaData.getRoot_gb();
            return new OpenstackNovaEvent(account, instanceId, type, memory, vcpus, Time.fromNovaTimeToMills(time), image, disk);
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
            String tenant = neutronEvent.get_context_tenant_id();
            String timestamp = neutronEvent.get_context_timestamp();
            return new OpenstackNeutronEvent(tenant, id, type, Time.fromOpenstackTimeToMills(timestamp));
        }

        return null;
    }

    private OpenstackCinderEvent manageCinderMessage(String content) {
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
            return new OpenstackCinderEvent(tenant, id, type, size, timestamp, null);
        }

        return null;
    }

    private String getType(String method){
        List<String> listOfActiveActions = Arrays.asList("spawning", "powering-on", "unpausing", "resuming",
                "floatingip.create.end", "volume.create.end", "resize_finish", "volume.resize.end");
        String paused = "pausing";
        String stopped="[powering-off]";
        String suspended = "suspending";
        List<String> listOfDeletedActions = Arrays.asList ("floatingip.delete.end", "volume.delete.end");

        if (listOfActiveActions.contains(method)){
            return settings.getOpenstackCollectorEventRun();
        }
        if (method.equals(paused)){
            return settings.getOpenstackCollectorEventPause();
        }
        if (method.equals(stopped)){
            return settings.getOpenstackCollectorEventStop();
        }
        if (method.equals(suspended)){
            return settings.getOpenstackCollectorEventSuspend();
        }
        if (listOfDeletedActions.contains(method)){
            return settings.getOpenstackCollectorEventDelete();
        }
        return method;
    }
}
