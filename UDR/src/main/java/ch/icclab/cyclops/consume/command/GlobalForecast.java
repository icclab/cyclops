package ch.icclab.cyclops.consume.command;
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
import ch.icclab.cyclops.dao.Usage;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.timeseries.forecast.ARIMAForecast;
import ch.icclab.cyclops.util.loggers.TimeSeriesLogger;
import ch.icclab.cyclops.consume.command.Forecast;
import org.jooq.InsertQuery;
import org.jooq.SelectQuery;
import java.util.*;
/**
 * Author: Panagiotis Gkikopoulos
 * Created: 17/06/2019
 * Description: Generate total revenue forecast command
 */
public class GlobalForecast extends Command {
    //Name of the target pricing model
    private String target;
    //Number of days in the future that the forecast will predict
    private long forecastSize;
    ArrayList<String> keysAsArray = new ArrayList<>();

    private class GenerateBill {
        String command;
        Long time_from;
        Long time_to;
        String request;

        GenerateBill(Long time_from, Long time_to, String request) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
            this.request = request;
        }
    }

    @Override
    Status execute() {
        Long time_from = System.currentTimeMillis();
        Long time_to = time_from + forecastSize * 86402000;
        Status status = new Status();
        //Retrieve usage records
        DbAccess db = new DbAccess();
        SelectQuery select = db.createSelectFrom(Usage.TABLE);
        //Sort by newest first
        select.addOrderBy(Usage.TIME_FIELD.desc());
        List<Usage> usages;
        usages = db.fetchUsingSelectStatement(select, Usage.class);
        // Generate the forecast
        List <String> users = countAccounts(usages);
        for(String user:users){
           status =  Forecast.compute(user,target,forecastSize);
        }
        return status;
    }
    private List<String> countAccounts(List<Usage> usages) {
        List<String> accounts = new ArrayList<>();
        for (Usage usage : usages) {
            if (!accounts.isEmpty()) {
                int i = 0;
                for (String account : accounts) {
                    if (account.equals(usage.getAccount())) i++;
                }
                if (i == 0) {
                    accounts.add(usage.getAccount());
                }
            } else {
                accounts.add(usage.getAccount());
            }
        }
        return accounts;
    }
}