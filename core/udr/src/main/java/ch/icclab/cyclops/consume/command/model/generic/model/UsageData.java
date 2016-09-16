package ch.icclab.cyclops.consume.command.model.generic.model;
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

import com.google.common.base.Objects;

import java.util.Map;

/**
 * Author: Skoviera
 * Created: 06/09/16
 * Description: Usage record coming from the database
 */
public class UsageData {
    private String _class;
    private String account;
    private Double usage;
    private Object unit;
    private Map metadata;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsageData usageData = (UsageData) o;
        return com.google.common.base.Objects.equal(_class, usageData._class) &&
                Objects.equal(account, usageData.account) &&
                Objects.equal(unit, usageData.unit) &&
                Objects.equal(metadata, usageData.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_class, account, unit, metadata);
    }

    public String get_class() {
        return _class;
    }
    public void set_class(String _class) {
        this._class = _class;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public Double getUsage() {
        return usage;
    }
    public void setUsage(Double usage) {
        this.usage = usage;
    }
    public void addToUsage(Double usage) {
        this.usage += usage;
    }

    public Object getUnit() {
        return unit;
    }
    public void setUnit(Object unit) {
        this.unit = unit;
    }

    public Map getMetadata() {
        return metadata;
    }
    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }
}
