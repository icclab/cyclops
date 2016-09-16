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

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.consume.command.model.generic.model.FlushData;
import ch.icclab.cyclops.publish.APICaller;
import ch.icclab.cyclops.util.loggers.CommandLogger;

import java.net.URL;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 09/09/16
 * Description: Generic Bill Request
 */
public class BillRequest extends Command {

    // TODO add service discovery
    private static String CDR_URL = "localhost:4568";
    private static String COIN_URL = "localhost:4571";

    // account holder for the bill
    private String account;

    // linked accounts in federated billing
    private List<String> linked;

    // time boundaries for the bill
    private Long from;
    private Long to;

    @Override
    protected Object execute() {
        try {
            // sanity checks first
            if (from == null || to == null || from < 0l || to <= from)
                return "[ERROR] invalid FROM and TO";
            if (account == null || account.isEmpty())
                return "[ERROR] invalid account name";

            CommandLogger.log("Received Bill Request that is meant for Billing Rule engine, therefore synchronously forwarding it");

            // perform CDR flush
            new APICaller().post(new URL(String.format("http://%s/command", CDR_URL)), new FlushData(from, to, account, linked));

            // request bill from Coin
            APICaller.Response response = new APICaller().post(new URL(String.format("http://%s/ruleengine/facts", COIN_URL)), this);
            return response.getAsList();

        } catch (Exception e) {
            return "No data";
        }
    }

    public String getAccount() {
        return account;
    }
    public List<String> getLinked() {
        return linked;
    }
    public Long getFrom() {
        return from;
    }
    public Long getTo() {
        return to;
    }
}
