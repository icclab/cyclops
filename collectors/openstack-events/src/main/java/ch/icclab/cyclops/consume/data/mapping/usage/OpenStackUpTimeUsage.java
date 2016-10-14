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
package ch.icclab.cyclops.consume.data.mapping.usage;

/**
 * Author: Oleksii
 * Date: 01/06/2016
 * Description: This class holds the OpenStackUpTimeUsage response
 */

public class OpenStackUpTimeUsage extends OpenStackUsage {

    public OpenStackUpTimeUsage(Long time, String account, String resourceId, Double usage, Double value, String className){
        this.account = account;
        this.usage = usage;
        this.time = time;
        this._class = className;
        this.metadata = new NovaMetadata(resourceId, value);
    }
    private String _class;
    private String unit = "sec";
    private NovaMetadata metadata;

    private class NovaMetadata {
        private Double value;
        private String  resourceId;
        NovaMetadata(String resourceId, Double value){
            this.resourceId = resourceId;
            this.value = value;
        }
    }

}
