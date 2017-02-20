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
import ch.icclab.cyclops.consume.data.mapping.usage.OpenStackUsage;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.*;
import ch.icclab.cyclops.util.Time;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import org.joda.time.DateTime;

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
    //influxdb health
    private InfluxDBHealth influxDBHealth;

    /**
     * Constructor has to be hidden
     */
    public OpenStackClient() {
        influxDBClient = new InfluxDBClient();
        settings = Loader.getSettings().getOpenstackSettings();
        publisherCredentials = Loader.getSettings().getPublisherCredentials();
        dbName = getDbName();
        messenger = Messenger.getInstance();
        classStructure=getUsageFormat();
        influxDBHealth = InfluxDBHealth.getInstance();
    }

    @Override
    public void run() {
        if (influxDBHealth.isHealthy()){
        transformEventsToUsageRecords();
        } else {
            SchedulerLogger.log("check database health");
        }
    }

    public abstract String getDbName();

    public abstract ArrayList generateValue(Long eventTime, OpenstackEvent lastEventInScope);

    public abstract ArrayList<Class> getListOfMeasurements();

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

        ArrayList<OpenStackUsage> eventList = new ArrayList<>();
        for (OpenstackTag source : resourceList) {
            try {
                DateInterval dates = new DateInterval(whenWasLastPull(source.getValue()));
                ArrayList<OpenStackUsage> udr = generateUDR(source.getValue(), dates);
                if (udr !=null){
                    eventList.addAll(udr);
                }
            } catch (Exception e) {
                SchedulerLogger.log("Couldn't generate UDR " +e);
            }
        }
        if (!eventList.isEmpty()) {
            BatchPointsContainer eventContainer = new BatchPointsContainer();
            for (OpenStackUsage event: eventList){
                eventContainer.addPoint(event.getPoint());
            }
            influxDBClient.persistContainer(eventContainer);

        }
        for (Class measurement : getListOfMeasurements()) {
            QueryBuilder listQuery = new QueryBuilder(measurement.getSimpleName()).and("wasSent", "false");
            try {
                List<OpenStackUsage> validUsages = influxDBClient.executeQuery(listQuery).getAsListOfType(measurement);
                Boolean status = false;
                if (!validUsages.isEmpty()){
                    for (OpenStackUsage event: validUsages){
                        event.setTime(event.getTime()*1000);
                    }
                    if (publisherCredentials.getPublisherByDefaultDispatchInsteadOfBroadcast()) {
                        status = messenger.publish(validUsages, OpenStackUsage.class.getSimpleName());
                    } else {
                        status = messenger.broadcast(validUsages);
                    }
                }
                if (status){
                    BatchPointsContainer eventContainer = new BatchPointsContainer();
                    for (OpenStackUsage event: validUsages){
                        event.isSent();
                        eventContainer.addPoint(event.getPoint());
                    }
                    influxDBClient.persistContainer(eventContainer);
                }
            } catch (Exception e) {
                SchedulerLogger.log("Couldn't generate UDR " +e);
            }
        }
            SchedulerLogger.log("All usage are published ");
    }


    /**
     * This class is being used to generate interval either from last point or epoch
     */
    private class DateInterval {
        private Long fromDate;
        private Long toDate;

        DateInterval(Long startTime) {
            fromDate = startTime;
            toDate =  (new DateTime().getMillis()/1000)*1000 ;
        }

        Long getFromDate() {
            return fromDate;
        }

        Long getToDate() {
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
        Long toMills = dates.getToDate();
        Long fromMills = dates.getFromDate();
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
            //some weird behaviour: first iteration from influxdb gets seconds, next ones milliseconds
            for (OpenstackEvent event: generatedEvents){
                if (event.getTime() < Math.pow(10, 12)) {
                    event.setTime(event.getTime()*1000);
                }
            }
            for (OpenstackEvent event : generatedEvents) {
                if (lastEventInScope!= null && !lastEventInScope.getType().equals(settings.getOpenstackCollectorEventDelete())) {
                    Long eventTime = Double.valueOf(event.getTime().toString()).longValue();
                    if (lastEventInScope.getTime() < fromMills){
                        lastEventInScope.setTime(fromMills);
                    }
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
    private Long whenWasLastPull(String source) {
        Long latestTime = Time.getMilisForTime(settings.getOpenstackFirstImport());
        for (Class measurment: getListOfMeasurements()){
            QueryBuilder parameterQuery = new QueryBuilder(measurment.getSimpleName()).where("metadata.sourceId", source).orderDesc().limit(1);
            try {
                List<OpenStackUsage> record = influxDBClient.executeQuery(parameterQuery).getAsListOfType(OpenStackUsage.class);
                Long recordTime = record.get(0).getTime()*1000;
                if (latestTime < recordTime){ latestTime = recordTime; }
            } catch (Exception e){
                SchedulerLogger.log("Influxdb data cannot be fetched. " + e);
            }
        }
       return latestTime;
    }
}
