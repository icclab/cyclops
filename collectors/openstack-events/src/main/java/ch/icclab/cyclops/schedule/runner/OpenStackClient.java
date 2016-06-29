/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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
package ch.icclab.cyclops.schedule.runner;

import ch.icclab.cyclops.consume.data.mapping.OpenStackEventUDR;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Updated by: Oleksii Serhiienko
 * Updated on: 01-July-16
 * Description: Client class for Telemetry. Asks underlying classes for CloudStack data and saves it
 */
public class OpenStackClient extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(OpenStackClient.class.getName());


    // connection to Load

    private HibernateClient hibernateClient;
    private static InfluxDBClient influxDBClient;
    private static OpenstackSettings settings;
    private String dbName;
    private static Messenger messenger;

    public OpenStackClient() {
        hibernateClient = HibernateClient.getInstance();
        influxDBClient = InfluxDBClient.getInstance();
        settings = Loader.getSettings().getOpenstackSettings();
        dbName = Loader.getSettings().getOpenstackSettings().getOpenstackEventTable();
        messenger = Messenger.getInstance();
    }


    @Override
    public void run() {
        transformEventsToUDRs();
    }

    /**
     * Transform Openstack Events to UDR records
     */
    private void transformEventsToUDRs() {

        QueryBuilder parameterQuery = new QueryBuilder(dbName).select();
        List<Map> tsdbData = influxDBClient.executeQuery(parameterQuery);

        HashMap<String, ArrayList<String>> clientInstanceMap = getInstanceIdsPerClientId(tsdbData);

        createUDRRecords(clientInstanceMap);
    }


    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     * and maps instanceIds to them which are saved to a HashMap.
     *
     * @param tsdbData
     * @return
     */
    private HashMap<String, ArrayList<String>> getInstanceIdsPerClientId(List<Map> tsdbData) {
        logger.trace("BEGIN HashMap<String,ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData)");
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        for (Map obj : tsdbData) {
            String clienId = obj.get("clientId").toString();
            String instanceId = obj.get("instanceId").toString();
            if (!map.containsKey(clienId)) {
                 map.put(clienId, new ArrayList<>());
            }
            if (!map.get(clienId).contains(instanceId)){
                map.get(clienId).add(instanceId);
            }
        }
        return map;
    }

    private void createUDRRecords(HashMap<String, ArrayList<String>> clientInstanceMap) {
        Iterator it = clientInstanceMap.entrySet().iterator();
        DateInterval dates = new DateInterval(whenWasLastPull());
        // get now
        Long time = new DateTime().withZone(DateTimeZone.UTC).getMillis();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String clientId = pair.getKey().toString();
            ArrayList<String> instanceIds = (ArrayList<String>) pair.getValue();
            for (String instanceId : instanceIds) {
                generateUDR(clientId, instanceId, dates);
            }
            it.remove();
        }

        // update time stamp
        LatestPullORM pull = (LatestPullORM) hibernateClient.getObject(LatestPullORM.class, 1l);
        if (pull == null) {
            pull = new LatestPullORM(time);
        } else {
            pull.setTimeStamp(time);
        }
        hibernateClient.persistObject(pull);

    }

    /**
     * This class is being used to generate interval either from last point or epoch
     */
    private class DateInterval {
        private String fromDate;
        private String toDate;

        protected DateInterval(DateTime from) {
            from.withZone(DateTimeZone.UTC);
            fromDate = from.toString("yyyy-MM-dd'T'hh:mm:ss");
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+00"));
            toDate = formatter.format(Calendar.getInstance().getTime());
        }

        protected String getFromDate() {
            return fromDate;
        }

        protected String getToDate() {
            return toDate;
        }
    }

    private boolean usageEvent(Map event) {
        return (event.get("status").equals(settings.getOpenstackCollectorEventResume()) ||
                event.get("status").equals(settings.getOpenstackCollectorEventSpawn()) ||
                event.get("status").equals(settings.getOpenstackCollectorEventUnpause()) ||
                event.get("status").equals(settings.getOpenstackCollectorEventStart()));
    }

    public void generateUDR(String clientId, String instanceId, DateInterval dates) {

        ArrayList<Map> generatedEvents = new ArrayList<>();
        // generate first event
        Long toMills = Time.getMilisForTime(dates.getToDate());
        Long fromMills = Time.getMilisForTime(dates.getFromDate());


        try {
            generatedEvents.add(getEventBeforeTime(fromMills, clientId, instanceId));
        } catch (Exception ignored){
        }
        //get all events

        QueryBuilder parameterQuery = new QueryBuilder(dbName).where("clientId", clientId).
                and("instanceId", instanceId).timeTo(toMills, MILLISECONDS).timeFrom(fromMills, MILLISECONDS);

        generatedEvents.addAll(influxDBClient.executeQuery(parameterQuery));

        // generate last event

        generatedEvents.add(getEventBeforeTime(toMills, clientId, instanceId));

        Double usageValues = 0.0;
        Map lastEventInScope = new HashMap<>();
        for (Map event: generatedEvents){
            if ((! lastEventInScope.isEmpty()) && (usageEvent(lastEventInScope))) {
                Double eventTime = makeValidTime(event.get("time").toString());
                Double eventLastTime = makeValidTime(lastEventInScope.get("time").toString());
                usageValues = usageValues + (eventTime - eventLastTime);
            }
            lastEventInScope = event;

        }

        OpenStackEventUDR generatedEvent = fromMapToUDR(clientId, instanceId, fromMills, lastEventInScope.get("status").toString(), usageValues);

        messenger.publish(generatedEvent, "");

    }

    public Double makeValidTime(String time){
        Double doubleTime = new Double(time);
        Double checkValue = new Double("1E10");
        if (checkValue < doubleTime){
            return doubleTime/1000;
        }
        return doubleTime;
    }

    public Map getEventBeforeTime (Long time, String clientId, String instanceId){
        QueryBuilder parameterQuery = new QueryBuilder(dbName).where("clientId", clientId).
                and("instanceId", instanceId).timeTo(time, MILLISECONDS);
        List<Map> ListEvents = influxDBClient.executeQuery(parameterQuery);
        Map lastEvent = ListEvents.get(ListEvents.size()-1);
        lastEvent.replace("time", time);

        return lastEvent;

    }


    public OpenStackEventUDR fromMapToUDR(String clientId, String instanceId, Long time, String action, Double usage){
        OpenStackEventUDR udr = new OpenStackEventUDR();
        udr.setClientId(clientId);
        udr.setInstanceId(instanceId);
        udr.setTime(time);
        udr.setStatus(action);
        udr.setUsage(usage);

        return udr;
    }


    private DateTime whenWasLastPull() {
        DateTime last;

        LatestPullORM pull = (LatestPullORM) HibernateClient.getInstance().getObject(LatestPullORM.class, 1l);
        if (pull == null) {
            last = new DateTime(0);
        } else {
            last = new DateTime(pull.getTimeStamp());
        }

        logger.trace("Getting the last pull date " + last.toString());

        // get date specified by admin
        String date = settings.getOpenstackFirstImport();
        if (date != null && !date.isEmpty()) {
            try {
                logger.trace("Admin provided us with import date preference " + date);
                DateTime selection = Time.getDateForTime(date);

                // if we are first time starting and having Epoch, change it to admin's selection
                // otherwise skip admin's selection and continue from the last DB entry time
                if (last.getMillis() == 0) {
                    logger.debug("Setting first import date as configuration file dictates.");
                    last = selection;
                }
            } catch (Exception ignored) {
                // ignoring configuration preference, as admin didn't provide correct format
                logger.debug("Import date selection for CloudStack ignored - use yyyy-MM-dd format");
            }
        }

        return last.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00")));
    }
}
