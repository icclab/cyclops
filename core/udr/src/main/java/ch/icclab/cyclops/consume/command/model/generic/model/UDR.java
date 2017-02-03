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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 06/09/16
 * Description: UDR output
 */
public class UDR {
    private String _class = getClass().getSimpleName();
    private String account;
    private Long time;
    private Long from;
    private Long to;
    private List data;

    public static List<String> getTagNames() {
        return Collections.singletonList("account");
    }

    public static String getTimeFieldName() {
        return "time";
    }

    public static TimeUnit getTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    public UDR(String account, Long from, Long to) {
        this.time = System.currentTimeMillis();
        this.account = account;
        this.from = from;
        this.to = to;
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

    public List getData() {
        return data;
    }
    public void setData(List data) {
        this.data = data;
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
}
