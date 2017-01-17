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

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackTag;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
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
    //custom event POJO
    private Class classStructure;

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
        classStructure=getUsageFormat();
    }

    @Override
    public void run() {
        transformEventsToUsageRecords();
    }

    public abstract String getDbName();

    public abstract ArrayList generateValue(Long eventTime, OpenstackEvent lastEventInScope);

    public abstract void updateLatestPull(Long time);

    public abstract DateTime getLatestPull();

    public abstract Class getUsageFormat();


    /**
     * Openstack event transformer
     * Generating Usage record
     * sending the message to the queue
     */
    private void transformEventsToUsageRecords() {
        SchedulerLogger.log("Scheduler has been started");
        QueryBuilder parameterQuery = QueryBuilder.getShowTagValuesQuery(dbName, "source");
        SchedulerLogger.log("Fetching all events from database ...");
        List<OpenstackTag> resourceList = new ArrayList<>();
        try {
            resourceList = influxDBClient.executeQuery(parameterQuery).getAsListOfType(OpenstackTag.class);
        } catch (Exception e){
            SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
        }
        SchedulerLogger.log("UDR creation process is started... ");
        DateInterval dates = new DateInterval(whenWasLastPull());
        SchedulerLogger.log("The last pull was " + dates.fromDate + " " + dates.toDate);
        Long time = Time.getMilisForTime(dates.getToDate());
        SchedulerLogger.log("Current timestamp is " + time);
        ArrayList<OpenStackUsage> eventList = new ArrayList<>();
        for (OpenstackTag source : resourceList) {
            try {
                ArrayList<OpenStackUsage> udr = generateUDR(source.getValue(), dates);
                if (udr !=null){
                    eventList.addAll(udr);
                }
            } catch (Exception e) {
                SchedulerLogger.log("Couldn't generate UDR " +e);
            }
        }
        if (!eventList.isEmpty()) {
            if (publisherCredentials.getPublisherByDefaultDispatchInsteadOfBroadcast()) {
                messenger.publish(eventList, OpenStackUsage.class.getSimpleName());
            } else {
                messenger.broadcast(eventList);
            }
            SchedulerLogger.log("All usage are published ");
            updateLatestPull(time);
        }
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

    /**
     * Method to generate UDR record for a source between two time points
     * @param source source id
     * @param dates DateInterval object
     * @return list of usages
     */
    private ArrayList<OpenStackUsage> generateUDR(String source, DateInterval dates) {

        ArrayList<OpenstackEvent> generatedEvents = new ArrayList<>();
        // generate first event
        Long toMills = Time.getMilisForTime(dates.getToDate());
        Long fromMills = Time.getMilisForTime(dates.getFromDate());
        Boolean isItExist = true;

        try {
            OpenstackEvent lastEvent = getEventBeforeTime(fromMills, source);
            if (lastEvent.getType().equals(settings.getOpenstackCollectorEventDelete())){
                isItExist = false;
            }
            else {
                generatedEvents.add(lastEvent);
            }
        } catch (Exception e){
            SchedulerLogger.log("No events for " + source + " before "+ dates.toDate);
        }

        if (isItExist) {
            //get all events
            ArrayList<OpenStackUsage> listOfUDRs= new ArrayList<>();
            QueryBuilder parameterQuery = new QueryBuilder(dbName).
                    and("source", source).timeTo(toMills, MILLISECONDS).timeFrom(fromMills, MILLISECONDS);
            try{
                generatedEvents.addAll(
                        influxDBClient.executeQuery(parameterQuery).getAsListOfType(classStructure));
            } catch (Exception e){
                SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
            }
            // generate last event
            generatedEvents.add(getEventBeforeTime(toMills, source));
            OpenstackEvent lastEventInScope = null;
            for (OpenstackEvent event : Time.normaliseInfluxDB(generatedEvents)) {
                if (lastEventInScope!= null && !lastEventInScope.getType().equals(settings.getOpenstackCollectorEventDelete())) {
                    Long eventTime = Double.valueOf(event.getTime().toString()).longValue();
                    listOfUDRs.addAll(generateValue(eventTime, lastEventInScope));
                }
                lastEventInScope = event;
            }
            return listOfUDRs;
        }
        return null;
    }

    /**
     * Method to get the last event before specific time
     * @param source source id
     * @param time in milliseconds
     * @return OpenstackEvent object
     */
    private OpenstackEvent getEventBeforeTime(Long time, String source){
        QueryBuilder parameterQuery = new QueryBuilder(dbName).
                and("source", source).timeTo(time, MILLISECONDS).orderDesc().limit(1);
        List<OpenstackEvent> listEvents = new ArrayList<>();
        try{
            listEvents = influxDBClient.executeQuery(parameterQuery).getAsListOfType(classStructure);
        } catch (Exception e){
            SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
        }
        OpenstackEvent lastEvent = listEvents.get(0);
        lastEvent.setTime(time);
        return lastEvent;
    }

    /**
     * Method to get the last pull time from postgresql
     * @return DateTime object
     */
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
