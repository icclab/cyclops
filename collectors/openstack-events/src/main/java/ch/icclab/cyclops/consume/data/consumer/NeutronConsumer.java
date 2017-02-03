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
import ch.icclab.cyclops.consume.data.mapping.messages.NeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNeutronEvent;
import ch.icclab.cyclops.util.Time;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;


/**
 * Author: Oleksii Serhiienko
 * Created: 12-Jan-17
 * Description: This class is responsible for managing Neutron messages
 */
public class NeutronConsumer extends AbstractConsumer {
    @Override
    protected OpenstackNeutronEvent manageMessage(String content) {
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
            String ipAdress = null;
            if (type.equals(settings.getOpenstackCollectorEventRun())){
                ipAdress = neutronEvent.getPayload().getFloatingip().getFloating_ip_address();
            }
            return new OpenstackNeutronEvent(tenant, id, ipAdress, type, Time.fromOpenstackTimeToMills(timestamp));
        }

        return null;
    }
}
