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
package ch.icclab.cyclops.model;

import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 15-Oct-15
 * Description: POJO object for Virtual Machine Usage Data, both type 1 (Running) and 2 (Allocated)
 */

public class VMAllocated extends UsageData {

    // The ID of the virtual machine
    private String virtualmachineid;

    // The name of the virtual machine
    private String name;

    // The ID of the template or the ID of the parent template. The parent template value is present when the current template was created from a volume.
    private String templateid;

    // Hypervisor
    private String type;

    /////////////////////////////
    // Getters and Setters

    public String getVirtualmachineid() {
        return virtualmachineid;
    }

    public void setVirtualmachineid(String virtualmachineid) {
        this.virtualmachineid = virtualmachineid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateid() {
        return templateid;
    }

    public void setTemplateid(String templateid) {
        this.templateid = templateid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected void additionalMetadata(Map map) {
        if (type != null)
            map.put("type", type);
        if (name != null)
            map.put("name", name);
        if (templateid != null)
            map.put("templateId", templateid);
        if (virtualmachineid != null)
            map.put("virtualmachineId", virtualmachineid);
    }
}