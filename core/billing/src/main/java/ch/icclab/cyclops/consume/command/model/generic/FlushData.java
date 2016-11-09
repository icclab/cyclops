package ch.icclab.cyclops.consume.command.model.generic;
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
import java.util.List;

/**
 * Author: Skoviera
 * Created: 14/09/16
 * Description: Flush Data request
 */
public class FlushData {
    private String _class = getClass().getSimpleName();
    private List<String> accounts;
    private Long from;
    private Long to;
    private Boolean sync;
    private Boolean output;

    public FlushData(Long from, Long to, String account, List accounts) {
        this.from = from;
        this.to = to;

        this.accounts = new ArrayList<>();
        this.accounts.add(account);

        // in case we have federated bill request
        if (accounts != null && !accounts.isEmpty())
            this.accounts.addAll(accounts);
    }

    public void enableSync() {
        sync = true;
    }
    public void disableSync() {
        sync = false;
    }

    public void enableOutput() {
        output = true;
    }
    public void disableOutput() {
        output = false;
    }
}
