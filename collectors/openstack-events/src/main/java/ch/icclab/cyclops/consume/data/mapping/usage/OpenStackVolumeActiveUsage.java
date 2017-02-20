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

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Oleksii
 * Date: 01/06/2016
 * Description: This class holds the OpenStackVolumeActiveUsage response
 */
public class OpenStackVolumeActiveUsage extends OpenStackUsage {
    public  OpenStackVolumeActiveUsage(){}
    public OpenStackVolumeActiveUsage(Long time, String account, String source, String sourceId,  Double usage, Double value){
        this.account = account;
        this.usage = usage;
        this.time = time;
        Map<String, String> my_metadata = new HashMap<>();
        my_metadata.put("source", source);
        my_metadata.put("sourceId", sourceId);
        my_metadata.put("value", value.toString());
        this.metadata = my_metadata;
    }

    private String unit = "sec";
    private String _class = getClass().getSimpleName();
    private Map metadata;

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }
}