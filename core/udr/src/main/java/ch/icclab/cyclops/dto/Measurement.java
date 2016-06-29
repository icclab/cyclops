package ch.icclab.cyclops.dto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 17/05/16
 * Description: ParentDTO for Measurement
 */
public class Measurement extends ParentDTO {
    private String measurement;
    private Map query = null;
    List<Map> data = new ArrayList<>();

    public Measurement(String measurement) {
        this.measurement = measurement;
    }

    public Measurement(String measurement, Map query) {
        this.measurement = measurement;

        if (query != null && !query.isEmpty()) {
            this.query = new HashMap<>(query);
        }
    }

    //======== Getters and Setters
    public String getMeasurement() {
        return measurement;
    }
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Map getQuery() {
        return query;
    }
    public void setQuery(Map query) {
        this.query = query;
    }

    public List<Map> getData() {
        return data;
    }
    public void setData(List<Map> data) {
        this.data = data;
    }
}
