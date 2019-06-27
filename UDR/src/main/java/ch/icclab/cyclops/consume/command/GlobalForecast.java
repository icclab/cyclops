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
 * Created: 17/06/2019
 * Description: Generate total revenue forecast command
 */
public class GlobalForecast extends Command{
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
        int generated = generateForecastRecords(usages);
        // Only continue if any forecast records were generated
        if (generated > 0) {
            //Generate virtual UDRs from the virtual Usage records
            GenerateUDRs generateUDRs = new GenerateUDRs();
            generateUDRs.setTime_from(time_from);
            generateUDRs.setTime_to(time_to);
            generateUDRs.setCommand(generateUDRs.getClass().getSimpleName());
            assert Loader.getSettings() != null;
            Messenger.publish(generateUDRs, Loader.getSettings().getPublisherCredentials().getRoutingKeyPublishUDRCommand());

            // Wait for CDRs to be generated
            for (String account : keysAsArray) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Generate Bill estimates

                GenerateBill generateBill = new GenerateBill(time_from, time_to, account + "-2D-arima-" + target);
                Messenger.publish(generateBill, "Billing");
                status.setSuccessful("Forecast estimation complete");

            }
        }
        return status;
    }

    /**
     * Generate usage and activity patterns and create forecast
     * @param usages List of usage records retrieved from the historical record
     * @return Number of forecast records generated
     */
    private int generateForecastRecords(List<Usage> usages){
        // Generate usage patterns
        HashMap<String,HashMap<String,List<Usage>>> usage_patterns = generateUsagePatterns(usages);
        // Generate daily activity pattern
        HashMap<Date,Integer> daily_activity = generateActivityPattern(usages);
        // Daily activity forecast
        // Create a list of user counts
        List<Integer> forecastInput = new ArrayList<>();
        for (Map.Entry<Date,Integer> entry : daily_activity.entrySet()) forecastInput.add(entry.getValue());
        //Generate activity forecast
        List<Integer> activityForecast = generateActivityForecast(forecastInput);
        // Generate daily Usage forecast
        // Counter of forecasts generated
        return generateTotalForecast(usage_patterns,activityForecast);
    }

    /**
     * Organize usage records recovered from DB by account and metric type
     * @param usages Records retrieved from DB
     * @return A map of usage patterns per account
     */
    private HashMap<String,HashMap<String,List<Usage>>> generateUsagePatterns(List<Usage> usages){
        // Group usages by user and type
        HashMap<String,HashMap<String,List<Usage>>> usage_patterns = new HashMap<>();
        // For each usage record
        for(Usage usage:usages){
            //if this account does not exist in the map
            if(!usage_patterns.containsKey(usage.getAccount())){
                // Make a new usage map for this account and add the current usage
                HashMap<String,List<Usage>> new_usage = new HashMap<>();
                List<Usage> temp = new_usage.computeIfAbsent(usage.getMetric(), k -> new ArrayList<>());
                temp.add(usage);
                usage_patterns.put(usage.getAccount(),new_usage);
            }
            //if the account already exists in the map
            else{
                // Retrieve the usage map for this user
                HashMap<String,List<Usage>> new_usage = usage_patterns.get(usage.getAccount());
                // Add this usage under the appropriate usage type
                List<Usage> temp = new_usage.computeIfAbsent(usage.getMetric(), k -> new ArrayList<>());
                temp.add(usage);
                usage_patterns.put(usage.getAccount(),new_usage);
            }
        }
        return usage_patterns;
    }

    /**
     * List the number of active accounts per day in the DB
     * @param usages Records retrieved from DB
     * @return A map of active accounts for each date of historical activity
     */
    private HashMap<Date,Integer> generateActivityPattern(List<Usage> usages){
        // Group usages by date
        HashMap<Date,List<Usage>> daily_usage = groupByDate(usages);
        // Find active accounts per date
        HashMap<Date,List<String>> daily_accounts = accountsPerDate(daily_usage);
        // Count active accounts per date
        HashMap<Date,Integer> daily_activity = new HashMap<>();
        //for each date
        for(Map.Entry<Date,List<String>> date : daily_accounts.entrySet()){
            // add the number of active accounts
            daily_activity.put(date.getKey(),date.getValue().size());
        }
        return daily_activity;
    }

    /**
     * Group records by date
     * @param usages Records retrieved from DB
     * @return Map of usages per date
     */
    private HashMap<Date,List<Usage>> groupByDate(List<Usage> usages){
        // Group usages by date
        HashMap<Date,List<Usage>> daily_usage = new HashMap<>();
        // For each usage record
        for(Usage usage : usages){
            // Get date of current record
            Date usage_date = new Date(usage.getTime());
            // add record under the date
            List<Usage> temp = daily_usage.computeIfAbsent(usage_date, k -> new ArrayList<>());
            temp.add(usage);
        }
        return daily_usage;
    }

    /**
     * List accounts that were active each date
     * @param daily_usage Usages grouped by date
     * @return Map of accounts active per date
     */
    private HashMap<Date,List<String>> accountsPerDate(HashMap<Date,List<Usage>> daily_usage){
        // Find active accounts per date
        HashMap<Date,List<String>> daily_accounts = new HashMap<>();
        //for each date
        for(Map.Entry<Date,List<Usage>> date : daily_usage.entrySet()){
            // Create entry for this date in new map
            daily_accounts.put(date.getKey(),new ArrayList<>());
            // for each record under this date
            for(Usage usage : date.getValue()){
                //if the account of this record had no other activity this date
                if(daily_accounts.get(date.getKey()).isEmpty()){
                    List<String> temp = daily_accounts.get(date.getKey());
                    temp.add(usage.getAccount());
                }
                for(String str: daily_accounts.get(date.getKey())){
                    if(!str.trim().contains(usage.getAccount())){
                        // add this account to active accounts for this date
                        List<String> temp = daily_accounts.get(date.getKey());
                        temp.add(usage.getAccount());
                    }
                }
            }
        }
        return daily_accounts;
    }
    /**
     * Generate a forecast usage record from the current pattern
     * @param usages Usages of the current user for the current metric type
     * @param counter Number of days after today that the record will be timestamped with
     * @return 1 or 0 to add to count of generated records
     */
    private int generateForecastFromUsage(List<Usage> usages, int counter, String account){
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
            double[] forecastData = forecast.getForecast(metrics, 9, 0, 9, 0, 0, 0, 0, 1);
            //Generate Usage records from forecast
            for (double entry : forecastData) {
                DbAccess dbn = new DbAccess();
                InsertQuery insert = dbn.createInsertInto(Usage.TABLE);
                insert.addValue(Usage.DATA_FIELD, String.format("{\"target\":\"%s\"}", target));
                insert.addValue(Usage.ACCOUNT_FIELD, account + "-2D-arima-" + target);
                insert.addValue(Usage.METRIC_FIELD, metric);
                insert.addValue(Usage.TIME_FIELD, System.currentTimeMillis() + counter * 86400000);
                insert.addValue(Usage.USAGE_FIELD, entry);
                insert.addValue(Usage.UNIT_FIELD, unit);
                dbn.executeInsertStatement(insert);
            }
            return 1;
        }
        else{return 0;}
    }

    /**
     * Forecast the number of users that will be active for each of the days of the forecast duration
     * @param input List of active users per day in the historical record
     * @return List of active users per day as predicted by the model
     */
    private List<Integer> generateActivityForecast(List<Integer> input){
        List<Integer> output = new ArrayList<>();
        if(input.size() >= 28){
            double[] metrics = new double[input.size()];
            for (int i = 0; i < input.size(); i++) metrics[i] = input.get(i);
            ARIMAForecast forecast = new ARIMAForecast();
            double[] forecastData = forecast.getForecast(metrics, 9, 0, 9, 0, 0, 0, 0, (int)(long)forecastSize);
            for(double count : forecastData) output.add((int) count);
            return output;
        }
        return output;
    }

    /**
     * Generate the usage forecast
     * @param usage_patterns The usage pattern of every known user by metric type
     * @param activityForecast The forecast number of users active for each day in the forecast duration
     * @return Number of generated records
     */
    private int generateTotalForecast(HashMap<String,HashMap<String,List<Usage>>> usage_patterns, List<Integer> activityForecast){
        // Counter of forecasts generated
        int generated = 0;
        // List of accounts with usage patterns
        keysAsArray = new ArrayList<>(usage_patterns.keySet());
        Random r = new Random();
        //For each day in the requested forecast length
        for(int i=0;i<forecastSize;i++){
            //get the number of active accounts for that day
            int activeAccounts = activityForecast.get(i);
            //for each active account
            for(int j=0;j<activeAccounts;j++){
                //select one of the usage patterns
                String key = keysAsArray.get(r.nextInt(keysAsArray.size()));
                HashMap<String,List<Usage>> user_pattern = usage_patterns.get(key);
                //for each usage type in the pattern
                for(String type : user_pattern.keySet()){
                    List<Usage> usageForecastInput = user_pattern.get(type);
                    //generate a single usage record for the next day in the forecast
                    //based on the existing pattern for this type
                    // for i days in the future
                    generated += generateForecastFromUsage(usageForecastInput,i,key);
                }
            }
        }
        return generated;
    }
}
