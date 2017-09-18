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
 * Description: This class holds the OpenStackUpTimeUsage response
 */

public class OpenStackObjectActiveUsage extends Usage {

    public OpenStackObjectActiveUsage(){}

    private String metric = getClass().getSimpleName();

    public OpenStackObjectActiveUsage(String account, String sourceId, String source, double value, String region){
        this.account = account;
        Map<String, String> my_data = new HashMap<>();
        my_data.put("source", source);
        my_data.put("sourceId", sourceId);
        my_data.put("value",  String.valueOf(value));
        my_data.put("region", region);
        this.data=my_data;
    }

    public OpenStackObjectActiveUsage clone() {
        String sourceId = this.data.get("sourceId").toString();
        String source = this.data.get("source").toString();
        double value = Double.parseDouble(this.data.get("value").toString());
        String region = this.data.get("region").toString();
        return new OpenStackObjectActiveUsage(this.account, sourceId, source, value, region);
    }

}
