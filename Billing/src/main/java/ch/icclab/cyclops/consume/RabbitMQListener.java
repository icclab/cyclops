package ch.icclab.cyclops.consume;
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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.ConsumerCredentials;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 25/01/16
 * Description: Implementation of RabbitMQ manager for consuming tasks
 */
public class RabbitMQListener {
    final static Logger logger = LogManager.getLogger(RabbitMQListener.class.getName());

    // this class has to be a singleton
    private static RabbitMQListener singleton = null;

    // our internal list of consumer tasks
    private List<ConsumerEntry> listOfConsumers = new ArrayList<>();

    // variables needed for RabbitMQ connection
    private Connection connection = null;

    /**
     * Constructor has to be hidden
     */
    private RabbitMQListener() {
        ConsumerCredentials credentials = Loader.getSettings().getConsumerCredentials();
        initialiseConnection(credentials);
    }

    /**
     * Access RabbitMQ object
     * @return singleton instance
     */
    public static RabbitMQListener getInstance() {
        if (singleton == null) {
            RabbitMQListener listener = new RabbitMQListener();

            if (listener.isConnected()) {
                singleton = listener;
            }
        }

        return singleton;
    }

    /**
     * Initialise connection to RabbitMQ
     */
    private void initialiseConnection(ConsumerCredentials credentials) {
        // connect to the RabbitMQ based on settings from Load
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(credentials.getConsumerUsername());
        factory.setPassword(credentials.getConsumerPassword());
        factory.setHost(credentials.getConsumerHost());
        factory.setPort(credentials.getConsumerPort());
        factory.setVirtualHost(credentials.getConsumerVirtualHost());
        factory.setAutomaticRecoveryEnabled(true);

        try {
            // create new connection
            connection = factory.newConnection();

            logger.trace(String.format("RabbitMQ Consumer connected to %s:%d", credentials.getConsumerHost(), credentials.getConsumerPort()));

        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Consumer couldn't be created: %s", e.getMessage()));
            connection = null;
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    /**
     * Add consumer for RabbitMQ
     * @param queue name
     * @param clazz consumer
     * @param prefetch number of messages
     * @return status
     */
    public Boolean addConsumer(String queue, AbstractConsumer clazz, int prefetch) {
        try {
            // create consumer and start listening
            ConsumerEntry consumer = new ConsumerEntry(clazz, connection, queue, prefetch);

            // add it to the list of consumers
            return listOfConsumers.add(consumer);
        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Consumer couldn't declare queue named \"%s\" because of: %s", queue, e.getMessage()));
            return false;
        }
    }

    /**
     * Stop consuming
     */
    private void stop() {
        try {
            // close individual channels
            for (ConsumerEntry entry: listOfConsumers) entry.shutDown();

            // remove consumers
            listOfConsumers.clear();

            // close the connection
            connection.close();
        } catch (Exception ignored) {}
    }

    /**
     * Shut down the singleton if it's running
     */
    protected static void shutDown() {
        if (singleton != null) {
            logger.trace("Shutting down RabbitMQ Consumer");
            singleton.stop();
            singleton = null;
        }
    }
}
