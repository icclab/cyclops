package ch.icclab.cyclops.consume.data;
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

import ch.icclab.cyclops.consume.ConsumerEntry;
import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.dao.event.OpenStackCinderEvent;
import ch.icclab.cyclops.dao.event.OpenStackNovaEvent;
import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.health.HealthCheck;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.DataLogger;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.jooq.SelectQuery;
import org.jooq.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/08/16
 * Description: Process data frame in runnable manner
 */
public abstract class EventProcess implements Runnable {
    protected static OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();
    private static String INVALID_RECORDS_ROUTING_KEY = "invalid_record";
    protected String content;

    private Long deliveryTag = null;
    private ConsumerEntry consumer = null;
    private boolean healthCheck = false;
    private Status status = new Status();
    protected DbAccess db = new DbAccess();

    /**
     * This is a method to transform message into OpenStackEvent object
     * @param content message itself
     * @return OpenStackEvent object
     */
    protected abstract List<OpenStackEvent> manageMessage(String content);

    public class Status {
        private boolean parsed = false;
        private DbAccess.PersistenceStatus persisted;
        private int numberOfRecords = 0;

        public boolean isParsed() {
            return parsed;
        }
        public void setParsed(int records) {
            numberOfRecords = records;
            parsed = true;
        }

        public void setPersisted(DbAccess.PersistenceStatus persisted) {
            this.persisted = persisted;
        }
        public boolean isPersisted() {
            return persisted == DbAccess.PersistenceStatus.OK;
        }
        public boolean isInvalid() {
            return persisted == DbAccess.PersistenceStatus.INVALID_RECORDS;
        }
        public boolean isDbDown() {
            return persisted == DbAccess.PersistenceStatus.DB_DOWN;
        }

        public int getNumberOfRecords() {
            return numberOfRecords;
        }
    }

    public EventProcess(String content) {
        this.content = content;
    }

    public EventProcess(String content, ConsumerEntry consumer, Long deliveryTag, boolean healthCheck) {
        this.content = content;
        this.consumer = consumer;
        this.deliveryTag = deliveryTag;
        this.healthCheck = healthCheck;
    }

    @Override
    public void run() {
        try {
            List<OpenStackEvent> events = manageMessage(content);
            // we got list of Usage objects
            boolean message_status = true;
            if (events != null) {
                // parsing was successful
                status.setParsed(1);
                for (OpenStackEvent event : events) {
                    // persist the usage data
                    DbAccess.PersistenceStatus persisted = persistEvent(event);
                    status.setPersisted(persisted);
                    switch (persisted) {
                        case DB_DOWN:
                            DataLogger.log("Received event, but unable to persist (db is down)");
                            nackAndScheduleHealthCheck();
                            message_status = false;
                            break;

                        case INVALID_RECORDS:
                            DataLogger.log("Received event, but some of them are invalid");
                            publishAndIfSuccessfulAck();
                            message_status = false;
                            break;

                        case OK:
                            DataLogger.log("Received and persisted event");
                            break;
                    }
                    if (!message_status) break;
                }
            } if (message_status) ackIfFromRabbitMQ();
        } catch (Exception ignored){  ackIfFromRabbitMQ();}

    }

    /**
     * Nack and optionally schedule a health check
     */
    private void nackAndScheduleHealthCheck() {
        if (deliveryTag != null) consumer.nackMessage(deliveryTag);
        if (healthCheck) TaskExecutor.getInstance().executeNow(new HealthCheck());
    }

    /**
     * ACK the message if it is coming from RabbitMQ, but publish it via an exchange
     * In case that publishing fails, NACK it and optionally schedule a health check
     */
    private void publishAndIfSuccessfulAck() {
        if (deliveryTag != null) {
            boolean published = Messenger.publish(content, INVALID_RECORDS_ROUTING_KEY);
            if (published) consumer.ackMessage(deliveryTag);
            else {
                consumer.nackMessage(deliveryTag);
                if (healthCheck) TaskExecutor.getInstance().executeNow(new HealthCheck());
            }
        }
    }

    /**
     * Ack a message if it is coming from RabbitMQ
     */
    private void ackIfFromRabbitMQ() {
        if (deliveryTag != null) consumer.ackMessage(deliveryTag);
    }

    /**
     * Persist list to database
     * @param event of event data
     * @return status
     */
    private DbAccess.PersistenceStatus persistEvent(OpenStackEvent event) {
        DbAccess db = new DbAccess();

        // persist the list transactionally
        return db.storePersistentObject(event);
    }

    /**
     * Was the processing and persisting successful
     * @return status
     */
    public Status getStatus() {
        return status;
    }


    private  <T> T getLastEvent(String source, Table table, Class<T> clazz) {
        try {
            // select value_attached from OpenStackNovaEvent table
            SelectQuery select = db.createSelectFrom(table);

            // source field selection
            select.addConditions(OpenStackEvent.SOURCE_FIELD.eq(source));

            // order by time
            select.addOrderBy(OpenStackEvent.TIME_FIELD.desc());

            // select the last record
            select.addLimit(1);

            // fetch and map into event

            return db.fetchUsingSelectStatement(select, clazz).get(0);
        } catch (Exception ignored){

        }
        return null;
    }

    protected OpenStackNovaEvent getLastNovaEvent(String source) {
        return getLastEvent(source, OpenStackNovaEvent.TABLE, OpenStackNovaEvent.class);
    }

    protected OpenStackCinderEvent getLastCinderEvent(String source) {
        return getLastEvent(source, OpenStackCinderEvent.TABLE, OpenStackCinderEvent.class);
    }



    /**
     * This is method to simplify openstatck event actions
     * @param method a method fetched from message
     * @return collector friendly method
     */
    protected String getType(String method){
        List<String> listOfActiveActions = Arrays.asList("spawning", "powering-on", "unpausing", "resuming",
                "floatingip.create.end", "volume.create.end", "resize_finish", "volume.resize.end");
        String paused = "pausing";
        String stopped="[powering-off]";
        String suspended = "suspending";
        List<String> listOfDeletedActions = Arrays.asList ("floatingip.delete.end", "volume.delete.end");

        if (listOfActiveActions.contains(method)){
            return settings.getOpenstackCollectorEventRun();
        }
        if (method.equals(paused)){
            return settings.getOpenstackCollectorEventPause();
        }
        if (method.equals(stopped)){
            return settings.getOpenstackCollectorEventStop();
        }
        if (method.equals(suspended)){
            return settings.getOpenstackCollectorEventSuspend();
        }
        if (listOfDeletedActions.contains(method)){
            return settings.getOpenstackCollectorEventDelete();
        }
        return method;
    }



}
