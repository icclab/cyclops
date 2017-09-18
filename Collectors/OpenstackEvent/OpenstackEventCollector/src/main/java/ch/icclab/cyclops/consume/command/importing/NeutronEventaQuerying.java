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
import ch.icclab.cyclops.dao.event.OpenStackNeutronEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import org.joda.time.DateTime;
import org.openstack4j.model.network.NetFloatingIP;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Serhiienko Oleksii
 * Created: 12/09/17
 * Description: Command for Querying Neutron Events in Openstack
 */
public class NeutronEventaQuerying extends AbstractOpenstackClient {

    @Override
    public List<OpenStackEvent> getObjectsToSafe(){
        ArrayList<NetFloatingIP> listOfIps  = new ArrayList<>();
        ArrayList<OpenStackEvent> listOfObjects = new ArrayList<>();
        listOfIps.addAll(session.networking().floatingip().list());
        long time = new DateTime().getMillis();
        OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();
        List<OpenStackNeutronEvent> sources = getSourceList(new OpenStackNeutronEvent());
        for(NetFloatingIP ip: listOfIps){
            String id = ip.getId();
            OpenStackNeutronEvent ipEvent = new OpenStackNeutronEvent(ip.getTenantId(), id, ip.getFloatingIpAddress(),
                    settings.getOpenstackCollectorEventRun(), time , settings.getOpenstackDefaultRegion());
            listOfObjects.add(ipEvent);

            for(OpenStackNeutronEvent source: sources){
                if(source.getSource().equals(id)){
                    sources.remove(source);
                    break;
                }
            }
        }
        for (OpenStackNeutronEvent source:sources){
            source.setTime(time);
            source.setType(settings.getOpenstackCollectorEventDelete());
            listOfObjects.add(source);
        }
        return listOfObjects;
    }
}
