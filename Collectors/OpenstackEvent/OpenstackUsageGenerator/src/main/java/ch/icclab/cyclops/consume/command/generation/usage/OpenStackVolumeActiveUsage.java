package ch.icclab.cyclops.consume.command.generation.usage;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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

public class OpenStackVolumeActiveUsage extends Usage {

    public  OpenStackVolumeActiveUsage(){}

    private String metric = getClass().getSimpleName();

    public OpenStackVolumeActiveUsage(long time, String account, String source, String sourceId, double usage,
                                      Double value, String region){
        this.account = account;
        this.usage = usage;
        this.time = time;
        Map<String, String> my_data = new HashMap<>();
        my_data.put("source", source);
        my_data.put("sourceId", sourceId);
        my_data.put("value", value.toString());
        my_data.put("region", region);
        this.data = my_data;
    }
}