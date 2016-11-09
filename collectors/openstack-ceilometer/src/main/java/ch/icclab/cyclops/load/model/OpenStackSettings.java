package ch.icclab.cyclops.load.model;

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

public class OpenStackSettings {
    private String supportedMeterList;
    private String ceilometerUrl;
    private String keystoneUrl;
    private String keystoneDomain;
    private String keystoneTenant;
    private String account;
    private String password;
    private String meterUrl;
    private String firstImport;

    public String getSupportedMeterList() {
        return supportedMeterList;
    }

    public void setSupportedMeterList(String supportedMeterList) {
        this.supportedMeterList = supportedMeterList;
    }

    public String getFirstImport() {
        return firstImport;
    }

    public void setFirstImport(String firstImport) {
        this.firstImport = firstImport;
    }

    public String getMeterUrl() {
        return meterUrl;
    }

    public void setMeterUrl(String meterUrl) {
        this.meterUrl = meterUrl;
    }

    public String getCeilometerUrl() {
        return ceilometerUrl;
    }

    public void setCeilometerUrl(String ceilometerUrl) {
        this.ceilometerUrl = ceilometerUrl;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeystoneUrl() {
        return keystoneUrl;
    }

    public void setKeystoneUrl(String keystoneUrl) {
        this.keystoneUrl = keystoneUrl;
    }

    public String getKeystoneTenant() {
        return keystoneTenant;
    }

    public void setKeystoneTenant(String keystoneTenant) {
        this.keystoneTenant = keystoneTenant;
    }

    public String getKeystoneDomain() {
        return keystoneDomain;
    }

    public void setKeystoneDomain(String keystoneDomain) {
        this.keystoneDomain = keystoneDomain;
    }
}
