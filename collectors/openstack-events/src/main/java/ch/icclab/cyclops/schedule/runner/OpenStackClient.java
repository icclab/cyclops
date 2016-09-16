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

import ch.icclab.cyclops.consume.data.mapping.udr.OpenStackUDR;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.Time;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Updated by: Oleksii Serhiienko
 * Updated on: 01-July-16
 * Description: Client class for transforming events to UDR Records for Openstack events
 */
public  abstract class OpenStackClient extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(OpenStackClient.class.getName());

    //link to hibernate
    protected HibernateClient hibernateClient;
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
        dbName = getDbName();
        messenger = Messenger.getInstance();
    }


    /**
     * runner for openstack events scheduler
     */
    @Override
    public void run() {
        transformEventsToUDRs();
    }

    public abstract String getDbName();

    public abstract OpenStackUDR generateValue(Long eventTime, Long eventLastTime, Map lastEventInScope, String instanceId);

    public abstract void updateLatestPull(Long time);

    public abstract DateTime getLatestPull();

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
        ArrayList<String> instanceList = getListOfInstances(data);
        SchedulerLogger.log("Map of client and instance IDs is done");
        createUDRRecords(instanceList);
    }

    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     *
     * @param data
     * @return
     */
    private  ArrayList<String> getListOfInstances(List<Map> data) {
        ArrayList<String> listOfInstances = new ArrayList<>();
        for (Map obj : data) {
            String instaceId = obj.get("instanceId").toString();
            if (!(listOfInstances.contains(instaceId))){
                listOfInstances.add(instaceId);
            }
        }
        return listOfInstances;
    }

    private void createUDRRecords(ArrayList<String> listOfInstances) {
        SchedulerLogger.log("UDR creation process is started... ");
        DateInterval dates = new DateInterval(whenWasLastPull());
        SchedulerLogger.log("The last pull was " + dates.fromDate + " " + dates.toDate);
        Long time = Time.getMilisForTime(dates.getToDate());
        SchedulerLogger.log("Current timestamp is " + time);
        ArrayList<OpenStackUDR> eventList = new ArrayList<>();
            for (String instanceId : listOfInstances) {
                try {
                    ArrayList<OpenStackUDR> udr = generateUDR(instanceId, dates);
                    if (udr !=null){
                        eventList.addAll(udr);
                    }
                } catch (Exception e) {
                    SchedulerLogger.log("Couldn't generate UDR " +e);
                }
        }

        if (publisherCredentials.getPublisherByDefaultDispatchInsteadOfBroadcast()) {
            messenger.publish(eventList, OpenStackUDR.class.getSimpleName());
        } else {
            messenger.broadcast(eventList);
        }
        SchedulerLogger.log("All udr are published ");
        updateLatestPull(time);
        // update time stamp
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

    private ArrayList<OpenStackUDR> generateUDR(String instanceId, DateInterval dates) {

        ArrayList<Map> generatedEvents = new ArrayList<>();
        // generate first event
        Long toMills = Time.getMilisForTime(dates.getToDate());
        Long fromMills = Time.getMilisForTime(dates.getFromDate());
        Boolean isItExist = true;

        try {
            Map lastEvent = getEventBeforeTime(fromMills, instanceId);
            if (lastEvent.get("type").equals(settings.getOpenstackCollectorEventDelete())){
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
            ArrayList<OpenStackUDR> listOfUDRs= new ArrayList<>();
            QueryBuilder parameterQuery = new QueryBuilder(dbName).
                    and("instanceId", instanceId).timeTo(toMills, MILLISECONDS).timeFrom(fromMills, MILLISECONDS);
            generatedEvents.addAll(Time.normaliseInfluxDB(influxDBClient.executeQuery(parameterQuery)));
            // generate last event
            generatedEvents.add(getEventBeforeTime(toMills, instanceId));
            Map lastEventInScope = new HashMap<>();
            for (Map event : generatedEvents) {
                if ((!lastEventInScope.isEmpty())) {
                    Long eventTime = Double.valueOf(event.get("time").toString()).longValue();
                    Long eventLastTime = Double.valueOf(lastEventInScope.get("time").toString()).longValue();
                    listOfUDRs.add(generateValue(eventTime, eventLastTime, lastEventInScope, instanceId));
                }
                lastEventInScope = event;
            }
            return listOfUDRs;
        }
        return null;
    }

    private Map getEventBeforeTime(Long time, String instanceId){
        QueryBuilder parameterQuery = new QueryBuilder(dbName).
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
        DateTime last = getLatestPull();
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
