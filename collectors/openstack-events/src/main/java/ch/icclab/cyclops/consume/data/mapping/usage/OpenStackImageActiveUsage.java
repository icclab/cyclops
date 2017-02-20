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
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenStackImageActiveUsage response
 */
public class OpenStackImageActiveUsage extends OpenStackUsage {
    public OpenStackImageActiveUsage(){}

    public OpenStackImageActiveUsage(Long time, String account, String source, String sourceId, String description, Double usage){
        this.account = account;
        this.usage = usage;
        this.time = time;
        Map<String, String> my_metadata = new HashMap<>();
        my_metadata.put("source", source);
        my_metadata.put("sourceId", sourceId);
        my_metadata.put("description", description);
        this.metadata=my_metadata;
    }

    private String unit = "sec";
    private String _class = getClass().getSimpleName();
    public Map<String, String> metadata;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

}

