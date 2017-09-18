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
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenStackFloatingIpActiveUsage response
 */

public class OpenStackFloatingIpActiveUsage extends Usage {

    public OpenStackFloatingIpActiveUsage(){}

    private String metric = getClass().getSimpleName();

    public OpenStackFloatingIpActiveUsage(long time, String account, String source, String sourceId, Double usage,
                                          String region){
        this.account = account;
        this.usage = usage;
        this.time = time;

        Map<String, String> my_data = new HashMap<>();
        my_data.put("source", source);
        my_data.put("sourceId", sourceId);
        my_data.put("region", region);
        this.data = my_data;
    }
}
