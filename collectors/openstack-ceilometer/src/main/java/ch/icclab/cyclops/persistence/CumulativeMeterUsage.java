package ch.icclab.cyclops.persistence;

import ch.icclab.cyclops.model.ceilometerMeasurements.AbstractOpenStackCeilometerUsage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 30/05/16.
 */

@Entity
public class CumulativeMeterUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double usageCounter;

    private String usageKey;

    public CumulativeMeterUsage() {
    }

    public CumulativeMeterUsage(AbstractOpenStackCeilometerUsage openStackUsage, String usageKey) {
        this.usageCounter = openStackUsage.getUsage();
        this.usageKey = usageKey;
    }

    public Double getUsageCounter() {
        return usageCounter;
    }

    public void setUsageCounter(Double usageCounter) {
        this.usageCounter = usageCounter;
    }

    public String getUsageKey() {
        return usageKey;
    }

    public void setUsageKey(String usageKey) {
        this.usageKey = usageKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
