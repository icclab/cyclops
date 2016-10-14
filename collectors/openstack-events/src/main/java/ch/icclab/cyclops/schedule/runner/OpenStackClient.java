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

import ch.icclab.cyclops.consume.data.mapping.usage.OpenStackUsage;
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
        influxDBClient = new InfluxDBClient();
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

    public abstract ArrayList generateValue(Long eventTime, Long eventLastTime, Map lastEventInScope, String resource);

    public abstract void updateLatestPull(Long time);

    public abstract DateTime getLatestPull();

    /*
     * Transform Openstack Events to UDR records
     */
    private void transformEventsToUDRs() {
        SchedulerLogger.log("Scheduler has been started");
        QueryBuilder parameterQuery = new QueryBuilder(dbName);
        SchedulerLogger.log("Fetching all events from database ...");
        List<Map> data = new ArrayList<>();
        try {
            data = influxDBClient.executeQuery(parameterQuery).getListOfObjects();
        } catch (Exception e){
            SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
        }
        SchedulerLogger.log("Influxdb data is successfully fetched.");
        SchedulerLogger.log("Making map of client and resources IDs...");
        ArrayList<String> resourceList = getListOfResources(data);
        SchedulerLogger.log("Map of client and resource IDs is done");
        createUDRRecords(resourceList);
    }

    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     *
     * @param data
     * @return
     */
    private  ArrayList<String> getListOfResources(List<Map> data) {
        ArrayList<String> listOfResources = new ArrayList<>();
        for (Map obj : data) {
            String resourceId = obj.get("resourceId").toString();
            if (!(listOfResources.contains(resourceId))){
                listOfResources.add(resourceId);
            }
        }
        return listOfResources;
    }

    private void createUDRRecords(ArrayList<String> listOfResources) {
        SchedulerLogger.log("UDR creation process is started... ");
        DateInterval dates = new DateInterval(whenWasLastPull());
        SchedulerLogger.log("The last pull was " + dates.fromDate + " " + dates.toDate);
        Long time = Time.getMilisForTime(dates.getToDate());
        SchedulerLogger.log("Current timestamp is " + time);
        ArrayList<OpenStackUsage> eventList = new ArrayList<>();
            for (String resourceId : listOfResources) {
                try {
                    ArrayList<OpenStackUsage> udr = generateUDR(resourceId, dates);
                    if (udr !=null){
                        eventList.addAll(udr);
                    }
                } catch (Exception e) {
                    SchedulerLogger.log("Couldn't generate UDR " +e);
                }
        }

        if (publisherCredentials.getPublisherByDefaultDispatchInsteadOfBroadcast()) {
            messenger.publish(eventList, OpenStackUsage.class.getSimpleName());
        } else {
            messenger.broadcast(eventList);
        }
        SchedulerLogger.log("All usage are published ");
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

    private ArrayList<OpenStackUsage> generateUDR(String resourceId, DateInterval dates) {

        ArrayList<Map> generatedEvents = new ArrayList<>();
        // generate first event
        Long toMills = Time.getMilisForTime(dates.getToDate());
        Long fromMills = Time.getMilisForTime(dates.getFromDate());
        Boolean isItExist = true;

        try {
            Map lastEvent = getEventBeforeTime(fromMills, resourceId);
            if (lastEvent.get("type").equals(settings.getOpenstackCollectorEventDelete())){
                isItExist = false;
            }
            else {
                generatedEvents.add(lastEvent);
            }
        } catch (Exception e){
            SchedulerLogger.log("No events for " + resourceId + " before "+ dates.toDate);
        }

        if (isItExist) {
            //get all events
            ArrayList<OpenStackUsage> listOfUDRs= new ArrayList<>();
            QueryBuilder parameterQuery = new QueryBuilder(dbName).
                    and("resourceId", resourceId).timeTo(toMills, MILLISECONDS).timeFrom(fromMills, MILLISECONDS);
            try{
                generatedEvents.addAll(Time.normaliseInfluxDB(influxDBClient.executeQuery(parameterQuery).getListOfObjects()));
            } catch (Exception e){
                SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
            }
            // generate last event
            generatedEvents.add(getEventBeforeTime(toMills, resourceId));
            Map lastEventInScope = new HashMap<>();
            for (Map event : generatedEvents) {
                if ((!lastEventInScope.isEmpty())) {
                    Long eventTime = Double.valueOf(event.get("time").toString()).longValue();
                    Long eventLastTime = Double.valueOf(lastEventInScope.get("time").toString()).longValue();
                    listOfUDRs.addAll(generateValue(eventTime, eventLastTime, lastEventInScope, resourceId));
                }
                lastEventInScope = event;
            }
            return listOfUDRs;
        }
        return null;
    }

    private Map getEventBeforeTime(Long time, String resourceId){
        QueryBuilder parameterQuery = new QueryBuilder(dbName).
                and("resourceId", resourceId).timeTo(time, MILLISECONDS);
        try {
            influxDBClient.executeQuery(parameterQuery);
        } catch (Exception e){
            logger.error("Couldn't execute DB request " + e);
        }
        List<Map> ListEvents = new ArrayList<>();
        try{
            ListEvents = influxDBClient.executeQuery(parameterQuery).getListOfObjects();
        } catch (Exception e){
            SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
        }
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
