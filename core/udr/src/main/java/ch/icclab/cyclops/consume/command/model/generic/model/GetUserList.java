package ch.icclab.cyclops.consume.command.model.generic.model;

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.InfluxDBResponse;
import ch.icclab.cyclops.timeseries.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * Created by Manu Perez on 15/11/16.
 */

public class GetUserList extends Command {
    private final String ACCOUNT_FIELD = "account";
    private final String MEASUREMENT_FIELD = "UDR";
    private final Integer LIMIT_FIELD = 1;
    private Long from;
    private Long to;

    @Override
    protected Object execute() {

        // sanity checks first
        if (from == null || to == null || from < 0l || to <= from) {
            return "[ERROR] invalid FROM and TO";
        }

        List<String> accounts = new ArrayList<>();

        // get 1 Udrs for each account in the specified time window
        List<UsageData> UDRs = getUDRs();

        // Add the accounts to the list.
        for (UsageData udr : UDRs)
            accounts.add(udr.getAccount());

        return accounts;
    }

    /**
     * Query database and list usage data for specified measurement
     *
     * @return UsageData list
     */
    private List<UsageData> getUDRs() {
        try {
            // construct and execute query
            QueryBuilder query = new QueryBuilder(MEASUREMENT_FIELD).groupBy(ACCOUNT_FIELD).limit(LIMIT_FIELD).timeFrom(from, TimeUnit.SECONDS).timeTo(to, TimeUnit.SECONDS);
            InfluxDBResponse response = new InfluxDBClient().executeQuery(query);

            // parse as list of UsageData
            return response.getAsListOfType(UsageData.class);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
}
