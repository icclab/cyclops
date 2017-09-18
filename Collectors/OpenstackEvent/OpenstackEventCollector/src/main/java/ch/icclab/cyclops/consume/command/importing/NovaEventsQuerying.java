package ch.icclab.cyclops.consume.command.importing;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import ch.icclab.cyclops.consume.command.AbstractOpenstackClient;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackNovaEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import org.joda.time.DateTime;
import org.openstack4j.model.compute.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Serhiienko Oleksii
 * Created: 12/09/17
 * Description: Command for Querying Nova Events in Openstack
 */
public class NovaEventsQuerying extends AbstractOpenstackClient{

    @Override
    public List<OpenStackEvent> getObjectsToSafe(){
        ArrayList<Server> listOfInstances  = new ArrayList<>();
        ArrayList<OpenStackEvent> listOfObjects = new ArrayList<>();
        listOfInstances.addAll(session.compute().servers().listAll(true));
        OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();
        ArrayList<String> valid_types = new ArrayList(Arrays.asList(settings.getOpenstackCollectorEventPause(),
                settings.getOpenstackCollectorEventRun(), settings.getOpenstackCollectorEventStop(),
                settings.getOpenstackCollectorEventSuspend()));
        long time = new DateTime().getMillis();
        List<OpenStackNovaEvent> sources = getSourceList(new OpenStackNovaEvent());
        for(Server server: listOfInstances){
            String type = server.getVmState();
            String id = server.getId();
            if(valid_types.contains(type)){
                OpenStackNovaEvent event = new OpenStackNovaEvent(server.getTenantId(), id, server.getName(), type,
                        (double) server.getFlavor().getRam(), (double) server.getFlavor().getVcpus(), time,
                        (double) server.getFlavor().getDisk(), (double) server.getFlavor().getEphemeral(),
                        server.getOsExtendedVolumesAttached().size(), server.getFlavor().getName(),
                        settings.getOpenstackDefaultRegion());
                listOfObjects.add(event);
            }
            for(OpenStackNovaEvent source: sources){
                if(source.getSource().equals(id)){
                    sources.remove(source);
                    break;
                }
            }
        }
        for (OpenStackNovaEvent source:sources){
            source.setTime(time);
            source.setType(settings.getOpenstackCollectorEventDelete());
            listOfObjects.add(source);
        }
        return listOfObjects;
    }

}

