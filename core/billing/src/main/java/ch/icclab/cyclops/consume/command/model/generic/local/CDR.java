package ch.icclab.cyclops.consume.command.model.generic.local;
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

import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 06/09/16
 * Description: CDR output
 */
public class CDR {
    private String _class = getClass().getSimpleName();
    private String account;
    private Long time;
    private Long from;
    private Long to;
    private Double charge;
    private List<Map> data;

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

    public Long getTime() {
        return time;
    }
    public void setTime(Long time) {
        this.time = time;
    }

    public Long getFrom() {
        return from;
    }
    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }
    public void setTo(Long to) {
        this.to = to;
    }

    public Double getCharge() {
        return charge;
    }
    public void setCharge(Double charge) {
        this.charge = charge;
    }

    public List<Map> getData() {
        return data;
    }
    public void setData(List<Map> data) {
        this.data = data;
    }
}
