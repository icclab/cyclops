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
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.Time;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Updated by: Oleksii Serhiienko
 * Updated on: 01-July-16
 * Description: Client class for transforming events to UDR Records for Openstack events
 */
public class OpenStackClient extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(OpenStackClient.class.getName());

    //link to hibernate
    private HibernateClient hibernateClient;
    //link to influxDB client
    private static InfluxDBClient influxDBClient;
    //Openstack settings
    private static OpenstackSettings settings;
    //link to database name
    private static PublisherCredentials publisherCredentials;
    private String dbName;
    //link to sending exchange
    private static Messenger messenger;

    /**
     * Constructor has to be hidden
     */
    public OpenStackClient() {
        hibernateClient = HibernateClient.getInstance();
        influxDBClient = InfluxDBClient.getInstance();
        settings = Loader.getSettings().getOpenstackSettings();
        publisherCredentials = Loader.getSettings().getPublisherCredentials();
        dbName = Loader.getSettings().getOpenstackSettings().getOpenstackEventTable();
        messenger = Messenger.getInstance();
    }


    /**
     * runner for openstack events scheduler
     */
    @Override
    public void run() {
        transformEventsToUDRs();
    }

    /*
     * Transform Openstack Events to UDR records
     */
    private void transformEventsToUDRs() {
        SchedulerLogger.log("Scheduler has been started");
        QueryBuilder parameterQuery = new QueryBuilder(dbName);
        SchedulerLogger.log("Fetching all events from database ...");
        List<Map> data = influxDBClient.executeQuery(parameterQuery);
        SchedulerLogger.log("Influxdb data is successfully fetched.");
        SchedulerLogger.log("Making map of client and instance IDs...");
        HashMap<String, ArrayList<String>> clientInstanceMap = getInstanceIdsPerClientId(data);
        SchedulerLogger.log("Map of client and instance IDs is done");
        createUDRRecords(clientInstanceMap);
    }


    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     * and maps instanceIds to them which are saved to a HashMap.
     *
     * @param data
     * @return
     */
    private HashMap<String, ArrayList<String>> getInstanceIdsPerClientId(List<Map> data) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        for (Map obj : data) {
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
        SchedulerLogger.log("UDR creation process is started... ");
        Iterator it = clientInstanceMap.entrySet().iterator();
        DateInterval dates = new DateInterval(whenWasLastPull());
        SchedulerLogger.log("The last pull was " + dates.fromDate + " " + dates.toDate);
        Long time = Time.getMilisForTime(dates.getToDate());
        SchedulerLogger.log("Current timestamp is " + time);
        ArrayList<OpenStackEventUDR> eventList = new ArrayList<OpenStackEventUDR>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String clientId = pair.getKey().toString();
            ArrayList<String> instanceIds = (ArrayList<String>) pair.getValue();
            for (String instanceId : instanceIds) {
                try {
                    ArrayList<OpenStackEventUDR> udr = generateUDR(clientId, instanceId, dates);
                    if (udr !=null){
                        eventList.addAll(udr);
                    }
                } catch (Exception e) {
                    SchedulerLogger.log("Couldn't generate UDR " +e);
                }

            }
            it.remove();
        }

        if (publisherCredentials.getPublisherByDefaultDispatchInsteadOfBroadcast()) {
            messenger.publish(eventList, OpenStackEventUDR.class.getSimpleName());
        } else {
            messenger.broadcast(eventList);
        }
        SchedulerLogger.log("All udr are published ");
        // update time stamp
        LatestPullORM pull = (LatestPullORM) hibernateClient.getObject(LatestPullORM.class, 1l);
        if (pull == null) {
            pull = new LatestPullORM(time);
        } else {
            pull.setTimeStamp(time);
        }
        SchedulerLogger.log("The last pull set to "+pull.getTimeStamp().toString());
        hibernateClient.persistObject(pull);

    }

    /**
     * This class is being used to generate interval either from last point or epoch
     */
    private class DateInterval {
        private String fromDate;
        private String toDate;

        DateInterval(DateTime from) {
            from.withZone(DateTimeZone.UTC);
            fromDate = from.toString("yyyy-MM-dd'T'HH:mm:ss");
            toDate =  DateTime.now(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss");
        }

        String getFromDate() {
            return fromDate;
        }

        String getToDate() {
            return toDate;
        }
    }

    private ArrayList<OpenStackEventUDR> generateUDR(String clientId, String instanceId, DateInterval dates) {

        ArrayList<Map> generatedEvents = new ArrayList<>();
        // generate first event
        Long toMills = Time.getMilisForTime(dates.getToDate());
        Long fromMills = Time.getMilisForTime(dates.getFromDate());
        Boolean isItExist = true;

        try {
            Map lastEvent = getEventBeforeTime(fromMills, clientId, instanceId);
            if (lastEvent.get("status").equals(settings.getOpenstackCollectorEventDelete())){
                isItExist = false;
            }
            else {
                generatedEvents.add(lastEvent);
            }
        } catch (Exception e){
            SchedulerLogger.log("No events for " + instanceId + " before "+ dates.toDate);
        }

        if (isItExist) {
            //get all events
            ArrayList<OpenStackEventUDR> listOfUDRs= new ArrayList<>();
            QueryBuilder parameterQuery = new QueryBuilder(dbName).where("clientId", clientId).
                    and("instanceId", instanceId).timeTo(toMills, MILLISECONDS).timeFrom(fromMills, MILLISECONDS);
            generatedEvents.addAll(Time.normaliseInfluxDB(influxDBClient.executeQuery(parameterQuery)));
            // generate last event
            generatedEvents.add(getEventBeforeTime(toMills, clientId, instanceId));
            Map lastEventInScope = new HashMap<>();
            for (Map event : generatedEvents) {
                if ((!lastEventInScope.isEmpty())) {
                    Long eventTime = Double.valueOf(event.get("time").toString()).longValue();
                    Long eventLastTime = Double.valueOf(lastEventInScope.get("time").toString()).longValue();
                    listOfUDRs.add(new OpenStackEventUDR(eventLastTime, clientId,
                            instanceId, lastEventInScope.get("status").toString(),
                            (double) (eventTime - eventLastTime) /1000)); //Seconds instead of milliseconds
                }
                lastEventInScope = event;
            }
            return listOfUDRs;
        }
        return null;
    }

    private Map getEventBeforeTime(Long time, String clientId, String instanceId){
        QueryBuilder parameterQuery = new QueryBuilder(dbName).where("clientId", clientId).
                and("instanceId", instanceId).timeTo(time, MILLISECONDS);
        try {
            influxDBClient.executeQuery(parameterQuery);
        } catch (Exception e){
            logger.error("Couldn't execute DB request " + e);
        }
        List<Map> ListEvents = influxDBClient.executeQuery(parameterQuery);
        Map lastEvent = ListEvents.get(ListEvents.size()-1);
        lastEvent.replace("time", time);
        return lastEvent;
    }

    private DateTime whenWasLastPull() {
        DateTime last;

        LatestPullORM pull = (LatestPullORM) HibernateClient.getInstance().getObject(LatestPullORM.class, 1l);
        if (pull == null) {
            last = new DateTime(0);
        } else {
            last = new DateTime(pull.getTimeStamp());
        }
        SchedulerLogger.log("Getting the last pull date " + last.toString());
        // get date specified by admin
        String date = settings.getOpenstackFirstImport();
        if (date != null && !date.isEmpty()) {
            try {
                SchedulerLogger.log("Admin provided us with import date preference " + date);
                DateTime selection = Time.getDateForTime(date);

                // if we are first time starting and having Epoch, change it to admin's selection
                // otherwise skip admin's selection and continue from the last DB entry time
                if (last.getMillis() == 0) {
                    SchedulerLogger.log("Setting first import date as configuration file dictates.");
                    last = selection;
                }
            } catch (Exception ignored) {
                // ignoring configuration preference, as admin didn't provide correct format
                SchedulerLogger.log("Import date selection for Openstastack ignored - use yyyy-MM-dd format");
            }
        }
        return last.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00")));
    }
}
