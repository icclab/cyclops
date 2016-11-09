package ch.icclab.cyclops.consume.data.mapping.usage;

/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

/**
 * Author: Oleksii
 * Date: 01/06/2016
 * Description: This class holds the OpenStackVolumeActiveUsage response
 */
public class OpenStackVolumeActiveUsage extends OpenStackUsage {
    public OpenStackVolumeActiveUsage(Long time, String account, String source, Double usage, Double value){
        this.account = account;
        this.usage = usage;
        this.time = time;
        this.metadata = new CinderMetadata(source, value);
    }

    private String unit = "sec";
    private String _class = getClass().getSimpleName();
    private CinderMetadata metadata;

    private class CinderMetadata {
        private String  source;
        private Double value;
        CinderMetadata(String source, Double value){
            this.source = source;
            this.value = value;
        }
    }
}