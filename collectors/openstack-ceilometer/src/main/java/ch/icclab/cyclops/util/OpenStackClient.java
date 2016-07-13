package ch.icclab.cyclops.util;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenStackSettings;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.Token;
import org.openstack4j.model.telemetry.Meter;
import org.openstack4j.model.telemetry.SampleCriteria;
import org.openstack4j.model.telemetry.Statistics;
import org.openstack4j.openstack.OSFactory;
import org.restlet.resource.ClientResource;

import java.util.List;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 30/05/16.
 */

public class OpenStackClient extends ClientResource{

    private OSClient osClient = buildClient();
    /**
     * Generates the token from Keystone service of OpenStack.
     * @return token A string consisting of Keystone token
     */
    public String generateToken() {
        Token token = osClient.getToken();
        return token.getId();
    }

    public OSClient buildClient(){
        OpenStackSettings settings = Loader.getSettings().getOpenStackSettings();
        String keystoneURL = settings.getKeystoneUrl();
        String keystoneUsername = settings.getAccount();
        String keystonePassword = settings.getPassword();
        String keystoneTenantName = settings.getKeystoneTenant();
        osClient = OSFactory.builder()
                .endpoint(keystoneURL)
                .credentials(keystoneUsername, keystonePassword)
                .tenantName(keystoneTenantName)
                .authenticate();
        return osClient;
    }

    public List<Statistics> getStatistics(String metername){

//        SampleCriteria criteria = new SampleCriteria();
//        criteria.add("groupby", SampleCriteria.Oper.EQUALS, "user_id");
//        criteria.add("groupby", SampleCriteria.Oper.EQUALS, "project_id");
//        criteria.add("groupby", SampleCriteria.Oper.EQUALS, "resource_id");

        List<Statistics> list = (List<Statistics>) osClient.telemetry().meters().statistics(metername);//,criteria);

        return null;
    }
}
