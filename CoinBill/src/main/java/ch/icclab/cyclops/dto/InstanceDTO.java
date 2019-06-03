package ch.icclab.cyclops.dto;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 19/02/16
 * Description: DTO POJO object for Instance
 */
public class InstanceDTO {

    private Long id;
    private Long templateId;
    private String name;
    private String rule;
    private String added;

    private Map parameters;

    public InstanceDTO() {}

    public InstanceDTO(Long id, String rule, Long templateId, String name, String added, Map parameters) {
        this.id = id;
        this.name = name;
        this.rule = rule;
        this.added = added;

        if (templateId != null) this.templateId = templateId;

        if (!parameters.isEmpty()) {
            this.parameters = parameters;
        }
    }

    //===== Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getRule() {
        return rule;
    }
    public void setRule(String rule) {
        this.rule = rule;
    }

    public Map getParameters() {
        return parameters;
    }
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String getAdded() {
        return added;
    }
    public void setAdded(String added) {
        this.added = added;
    }
}
