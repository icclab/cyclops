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
 * Created: 08/09/16
 * Description: Charge data for CDR record
 */
public class ChargeData {
    private String _class;
    private String account;
    private Double usage;
    private Double charge;
    private Map metadata;

    public ChargeData() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargeData that = (ChargeData) o;
        return Objects.equal(_class, that._class) &&
                Objects.equal(account, that.account) &&
                Objects.equal(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_class, account, metadata);
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

    public Double getCharge() {
        return charge;
    }
    public void setCharge(Double charge) {
        this.charge = charge;
    }

    public Map getMetadata() {
        return metadata;
    }
    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }
}
