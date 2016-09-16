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

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 14-Oct-15
 * Description: POJO object for IP Usage Data (type 2)
 */
public class IP extends UsageData {

    // Whether source NAT is enabled for the IP address
    private boolean issourcenat;

    // True if the IP address is elastic.
    private boolean issystem;

    /////////////////////////////
    // Getters and Setters

    public boolean issourcenat() {
        return issourcenat;
    }

    public void setIssourcenat(boolean issourcenat) {
        this.issourcenat = issourcenat;
    }

    public boolean issystem() {
        return issystem;
    }

    public void setIssystem(boolean issystem) {
        this.issystem = issystem;
    }

    @Override
    protected void additionalMetadata(Map map) {
        // nothing to do
    }
}
