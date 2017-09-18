package ch.icclab.cyclops.consume.command.generation.runner.openstack;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import ch.icclab.cyclops.consume.command.AbstractGeneration;
import ch.icclab.cyclops.consume.command.generation.usage.Usage;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.jooq.*;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Updated by: Oleksii Serhiienko
 * Updated on: 01-July-16
 * Description: Client class for transforming events to Usage Records for Openstack events
 */
public  abstract class OpenStackAbstractGeneration extends AbstractGeneration {

    //custom event POJO
    private  OpenStackEvent clazz = getEventFormat();

    public abstract ArrayList generateValue(long eventTime, OpenStackEvent lastEventInScope);

    public abstract OpenStackEvent getEventFormat();

    public abstract Class getUsageFormat();


    /**
     * Openstack event transformer
     * Generating Usage record
     * sending the message to the queue
     */
    @Override
    public ArrayList<Usage> generateUsageRecords() {
        String message;
        List resourceList = getSourceList();
        if (resourceList != null){
            ArrayList<Usage> eventList = new ArrayList<>();
            for (Object source : resourceList) {
                try {
                    OpenStackEvent source_mod = (OpenStackEvent) source;
                    ArrayList<Usage> usage = generateUsage(source_mod.getSource());
                    if (usage !=null) eventList.addAll(usage);
                    else return null;
                } catch (Exception e) {
                    message = String.format("Usage records for %s cannot be created", usageClass.getSimpleName());
                    CommandLogger.log(message);
                    status.setServerError(message);
                    return null;
                }
            } return eventList;
        }
        message = String.format("There are no sources in DB available for %s", usageClass.getSimpleName());
        CommandLogger.log(message);
        status.setServerError(message);
        return null;
    }

    /**
     * Method to get event record for a source between two time points
     * @param source source id
     * @return list of usages
     */
    private ArrayList<Usage> generateUsage(String source) {

        ArrayList<OpenStackEvent> generatedEvents = new ArrayList<>();
        // generate first event

        OpenStackEvent lastEvent = getEventBeforeTime(time_from, source);
        if (lastEvent != null){ generatedEvents.add(lastEvent); }
            //get all events
        ArrayList<Usage> listOfUsages= new ArrayList<>();
        generatedEvents.addAll(getEventsBetweenTime(time_from, time_to, source, clazz.getClass()));
        // generate last event
        generatedEvents.add(getEventBeforeTime(time_to, source));
        OpenStackEvent lastEventInScope = null;
        for (OpenStackEvent event : generatedEvents) {
            if (lastEventInScope!= null && !lastEventInScope.getType().equals(
                    settings.getOpenstackSettings().getOpenstackCollectorEventDelete())) {
                long eventTime = event.getTime();
                if (lastEventInScope.getTime() < time_from){
                    lastEventInScope.setTime(time_from);
                }
                listOfUsages.addAll(generateValue(eventTime, lastEventInScope));
            }
            lastEventInScope = event;
        }
        return listOfUsages;
    }

    /**
     * Method to get the last event before specific time
     * @param source source id
     * @param time in milliseconds
     * @return OpenStackEvent object
     */
    private OpenStackEvent getEventBeforeTime(long time, String source){
        try{
            // select time_from OpenStackEvent table
            SelectQuery select = db.createSelectFrom(clazz.getTable());

            // source field selection
            select.addConditions(OpenStackEvent.SOURCE_FIELD.eq(source));

            // time field selection
            select.addConditions(OpenStackEvent.TIME_FIELD.lt(inline(new Timestamp(time))));

            // select the last record
            select.addLimit(1);

            OpenStackEvent lastEvent = db.fetchUsingSelectStatement(select, clazz.getClass()).get(0);

            if (lastEvent.getType().equals(settings.getOpenstackSettings().getOpenstackCollectorEventDelete())){
                if(updateDeletedEvents(source)==-1){
                    String message = String.format("It was not possible to update event for %s", usageClass.getSimpleName());
                    CommandLogger.log(message);
                    status.setServerError(message);
                    return null;
                }
            }

            lastEvent.setTime(time);

            return lastEvent;
        } catch (Exception ignored) { return null; }
    }

    /**
     * Method to get events between specific times
     * @param source source id
     * @param from,to in milliseconds
     * @return List<OpenStackEvent></OpenStackEvent> object
     */
    private <T> List<T> getEventsBetweenTime(long from, long to, String source, Class<T> format){
        try{
            // select time_from OpenStackEvent table
            SelectQuery select = db.createSelectFrom(clazz.getTable());

            // source field selection
            select.addConditions(OpenStackEvent.SOURCE_FIELD.eq(source));
            // time field selection
            select.addConditions(OpenStackEvent.TIME_FIELD.gt(inline(new Timestamp(from))));

            // time field selection
            select.addConditions(OpenStackEvent.TIME_FIELD.lt(inline(new Timestamp(to))));

            // order by time
            select.addOrderBy(OpenStackEvent.TIME_FIELD.asc());

            return db.fetchUsingSelectStatement(select, format);
        } catch (Exception ignored) { return null; }
    }

    /**
     * Method mark all events for source as processed
     * @param source source id
     */
    private int updateDeletedEvents(String source){
        UpdateQuery update = db.createUpdateQuery(clazz.getTable());

        update.addValue(OpenStackEvent.PROCESSED_FIELD,true);

        update.addConditions(OpenStackEvent.SOURCE_FIELD.eq(source));

        return db.executeStatement(update);
    }

    /**
     * Method to get all unique sources
     */
    private List getSourceList(){
        try{
            // select time_from OpenStackEvent table
            SelectQuery select = db.createSelectFrom(clazz.getTable());

            select.addSelect(OpenStackEvent.SOURCE_FIELD);

            if (fast) select.addConditions(OpenStackEvent.PROCESSED_FIELD.eq(false));

            select.setDistinct(true);

            return db.fetchUsingSelectStatement(select, clazz.getClass());
        } catch (Exception ignored) { return null; }
    }
}
