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

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Oleksii
 * Date: 01/06/2016
 * Description: This class holds the OpenStackUpTimeUsage response
 */

public class OpenStackUpTimeUsage extends OpenStackUsage {

    public OpenStackUpTimeUsage(){}

    public OpenStackUpTimeUsage(Long time, String account, String sourceId, String source, Double usage, Double value, String className){
        this.account = account;
        this.usage = usage;
        this.time = time;
        this._class = className;
        Map<String, String> my_metadata = new HashMap<>();
        my_metadata.put("source", source);
        my_metadata.put("sourceId", sourceId);
        my_metadata.put("value", value.toString());
        this.metadata=my_metadata;
    }
    private String _class;
    private String unit = "sec";
    public Map metadata;

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public Map getMetadata() {
        return metadata;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
