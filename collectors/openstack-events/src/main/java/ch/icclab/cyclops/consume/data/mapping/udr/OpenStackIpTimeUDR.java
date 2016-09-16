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
package ch.icclab.cyclops.consume.data.mapping.udr;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenStackIpTimeUDR response
 */

public class OpenStackIpTimeUDR extends OpenStackUDR {
    public OpenStackIpTimeUDR(Long time, String account, String instanceId, String state, Double usage){
        this.account = account;
        this.usage = usage;
        this.time = time;
        this.metadata = new NeutronMetadata(instanceId, state);
    }

    private String unit = "sec";
    private String charType = "Infogram";
    private String _class = getClass().getSimpleName();
    private NeutronMetadata metadata;

    private class NeutronMetadata {
        private String  instanceId;
        private String state;
        NeutronMetadata(String instanceId, String state){
            this.instanceId = instanceId;
            this.state = state;
        }
    }
}
