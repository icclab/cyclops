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
import ch.icclab.cyclops.dao.Bill;
import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.health.HealthCheck;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.DataLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.gsonfire.GsonFireBuilder;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/08/16
 * Description: Process data frame in runnable manner
 */
public class BillProcess implements Runnable {
    private static String INVALID_RECORDS_ROUTING_KEY = "invalid_record";
    private String content;

    private Long deliveryTag = null;
    private ConsumerEntry consumer = null;
    private boolean healthCheck = false;
    private Status status = new Status();

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

    public BillProcess(String content) {
        this.content = content;
    }

    public BillProcess(String content, ConsumerEntry consumer, Long deliveryTag, boolean healthCheck) {
        this.content = content;
        this.consumer = consumer;
        this.deliveryTag = deliveryTag;
        this.healthCheck = healthCheck;
    }

    @Override
    public void run() {
        // parse JSON into list of Bill objects
        List<Bill> parsed = parseWhatCouldBeJSONObject(content);

        // in case we received just one entry and not an array
        if (parsed == null) parsed = parseWhatCouldBeJSONList(content);

        // we got list of Bill objects
        if (parsed != null && !parsed.isEmpty()) {
            // parsing was successful
            status.setParsed(parsed.size());

            // persist the Bill data
            DbAccess.PersistenceStatus persisted = persistList(parsed);
            status.setPersisted(persisted);

            switch (persisted) {
                case DB_DOWN:
                    DataLogger.log(String.format("Received %d metrics, but unable to persist (db is down)", parsed.size()));
                    nackAndScheduleHealthCheck();
                    break;

                    case INVALID_RECORDS:
                    DataLogger.log(String.format("Received %d metrics, but some of them are invalid", parsed.size()));
                    publishAndIfSuccessfulAck();
                    break;

                case OK:
                    DataLogger.log(String.format("Received and persisted %d metrics", parsed.size()));
                    ackIfFromRabbitMQ();
                    break;
            }
        } else {
            DataLogger.log("Received a message that is either invalid or empty");
            publishAndIfSuccessfulAck();
        }
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
     * @param list of Bill data
     * @return status
     */
    private DbAccess.PersistenceStatus persistList(List<Bill> list) {
        DbAccess db = new DbAccess();

        // persist the list transactionally
        return db.storePersistentObjects(list);
    }

    /**
     * Parse JSON into list of Bill objects
     * @param json text as array
     * @return list or null
     */
    private List<Bill> parseWhatCouldBeJSONList(String json) {
        try {
            Type billType = new TypeToken<List<Bill>>(){}.getType();
            return getNewGson().fromJson(json, billType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse JSON into list of Bill objects
     * @param json text as object
     * @return list or null
     */
    private List<Bill> parseWhatCouldBeJSONObject(String json) {
        try {
            Bill parsed = getNewGson().fromJson(json, Bill.class);
            return Collections.singletonList(parsed);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Was the processing and persisting successful
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get new Gson instance with registered processor that handles Bill object
     * @return Gson
     */
    private Gson getNewGson() {
        return new GsonFireBuilder().registerPreProcessor(Bill.class, new BillDeserializer<>()).createGson();
    }

}
