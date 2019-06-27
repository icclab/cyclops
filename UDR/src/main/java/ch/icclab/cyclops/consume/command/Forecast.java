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
import org.jooq.InsertQuery;
import org.jooq.SelectQuery;
import java.util.*;
/**
 * Author: Panagiotis Gkikopoulos
 * Created: 06/06/2019
 * Description: Generate revenue forecast command
 */
public class Forecast extends Command{
    private String account = "global";
    private String target;
    private long forecastSize;

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
        //Filter by specified account
        if(!(account.equals("global"))) {
            select.addConditions(Usage.ACCOUNT_FIELD.eq(account));
        }
        //Sort by newest first
        select.addOrderBy(Usage.TIME_FIELD.desc());
        List<Usage> usages;
        usages = db.fetchUsingSelectStatement(select, Usage.class);
        // Group usages by metric
        HashMap<String,List<Usage>> usage_map = new HashMap<>();
        for(Usage usage:usages){
            List<Usage> temp = usage_map.computeIfAbsent(usage.getMetric(), k -> new ArrayList<>());
            temp.add(usage);
        }
        // Generate forecast by usage type
        int generated = 0;
        for(String key:usage_map.keySet()){
            generated += generateForecastFromUsage(usage_map.get(key));
        }
        // Only continue if any forecast records were generated
        if(generated > 0) {
            //Generate virtual UDRs from the virtual Usage records
            GenerateUDRs generateUDRs = new GenerateUDRs();
            generateUDRs.setTime_from(time_from);
            generateUDRs.setTime_to(time_to);
            generateUDRs.setCommand(generateUDRs.getClass().getSimpleName());
            assert Loader.getSettings() != null;
            Messenger.publish(generateUDRs, Loader.getSettings().getPublisherCredentials().getRoutingKeyPublishUDRCommand());
            // Wait for CDRs to be generated
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Generate Bill estimates

            GenerateBill generateBill = new GenerateBill(time_from, time_to, account + "-arima-" + target);
            Messenger.publish(generateBill, "Billing");
            status.setSuccessful("Forecast estimation for account " + account + " complete");
        }
        else{status.setServerError("Not enough records found to generate forecast");}
        return status;
    }
    private int generateForecastFromUsage(List<Usage> usages){
        if (usages.size()>0) {

            String metric = usages.get(0).getMetric();
            String unit = usages.get(0).getUnit();
            if (usages.size() < 28) {
                TimeSeriesLogger.log("Insufficient data for metric " + metric);
                return 0;
            }
            double[] metrics = new double[usages.size()];
            for (int i = 0; i < usages.size(); i++) {
                metrics[i] = usages.get(i).getUsage();
            }
            //Generate forecast with ARIMA model
            ARIMAForecast forecast = new ARIMAForecast();
            double[] forecastData = forecast.getForecast(metrics, 9, 0, 9, 0, 0, 0, 0, (int)(long)forecastSize);
            //Generate Usage records from forecast
            int k = 0;
            for (double entry : forecastData) {
                DbAccess dbn = new DbAccess();
                InsertQuery insert = dbn.createInsertInto(Usage.TABLE);
                insert.addValue(Usage.DATA_FIELD, String.format("{\"target\":\"%s\"}", target));
                insert.addValue(Usage.ACCOUNT_FIELD, account + "-arima-" + target);
                insert.addValue(Usage.METRIC_FIELD, metric);
                insert.addValue(Usage.TIME_FIELD, System.currentTimeMillis() + k * 86400000);
                insert.addValue(Usage.USAGE_FIELD, entry);
                insert.addValue(Usage.UNIT_FIELD, unit);
                dbn.executeInsertStatement(insert);
                k++;
            }
            return 1;
        }
        else{return 0;}
    }
}
