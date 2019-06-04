package ch.icclab.cyclops.load.model;
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
public class RollbackEndpoints {
    // These fields correspond with the configuration file
    private String udrendpoint;
    private String cdrendpoint;

    public String getUdrendpoint() {
        return udrendpoint;
    }

    public void setUdrendpoint(String udrendpoint) {
        this.udrendpoint = udrendpoint;
    }

    public String getCdrendpoint() {
        return cdrendpoint;
    }

    public void setCdrendpoint(String cdrendpoint) {
        this.cdrendpoint = cdrendpoint;
    }

    public String getBillingendpoint() {
        return billingendpoint;
    }

    public void setBillingendpoint(String billingendpoint) {
        this.billingendpoint = billingendpoint;
    }

    private String billingendpoint;

}
