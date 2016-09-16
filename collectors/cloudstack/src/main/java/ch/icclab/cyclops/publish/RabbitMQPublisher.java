package ch.icclab.cyclops.publish;
/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.util.PrettyGson;
import ch.icclab.cyclops.util.loggers.DispatchLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Skoviera
 * Created: 13/04/16
 * Description: RabbitMQ publisher
 */
public class RabbitMQPublisher {
    final static Logger logger = LogManager.getLogger(RabbitMQPublisher.class.getName());

    // this class has to be a singleton
    private static RabbitMQPublisher singleton = null;

    // credentials to be used with RabbitMQ
    private PublisherCredentials credentials;

    // connection details
    private Connection connection;
    private Channel channel;

    /**
     * Constructor has to be hidden
     */
    private RabbitMQPublisher(PublisherCredentials cred) {
        this.credentials = cred;
    }

    /**
     * Access RabbitMQ object
     * @return singleton instance
     */
    public static RabbitMQPublisher getInstance() { return singleton; }


    /**
     * Create RabbitMQ instance
     * @param cred credentials
     * @return instance or null
     */
    public static RabbitMQPublisher createInstance(PublisherCredentials cred){
        if (singleton == null) {
            RabbitMQPublisher rabbitMQ = new RabbitMQPublisher(cred);

            // initialise connection and finalise singleton creation
            if (rabbitMQ.initialiseConnection()) {
                singleton = rabbitMQ;
            }
        }

        return singleton;
    }

    /**
     * Initialise connection to RabbitMQ
     * @return status
     */
    private Boolean initialiseConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(credentials.getPublisherUsername());
            factory.setPassword(credentials.getPublisherPassword());
            factory.setHost(credentials.getPublisherHost());
            factory.setPort(credentials.getPublisherPort());
            factory.setVirtualHost(credentials.getPublisherVirtualHost());

            connection = factory.newConnection();
            channel = connection.createChannel();

            // declare exchange to be used (we want it to be durable and based on routing key)
            channel.exchangeDeclare(credentials.getPublisherDispatchExchange(), "direct", true);
            channel.exchangeDeclare(credentials.getPublisherBroadcastExchange(), "fanout", true);

            logger.trace(String.format("RabbitMQ Publisher connected to %s:%d", credentials.getPublisherHost(), credentials.getPublisherPort()));
            logger.trace(String.format("RabbitMQ Publisher will dispatch to \"%s\" and broadcast to \"%s\" exchanges", credentials.getPublisherDispatchExchange(), credentials.getPublisherBroadcastExchange()));

            return true;
        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Publisher couldn't be created: %s", e.getMessage()));
            return false;
        }
    }

    /**
     * Publish a message to RabbitMQ
     * @param content to be send
     * @param routing key
     */
    protected void publish(Object content, String routing) {
        send(credentials.getPublisherDispatchExchange(), content, routing);
    }

    /**
     * Broadcast a message to RabbitMQ
     * @param content to be send
     */
    protected void broadcast(Object content) {
        send(credentials.getPublisherBroadcastExchange(), content, "");
    }

    /**
     * Send a message to RabbitMQ
     * @param exchange to be used
     * @param content to be sent
     * @param routing to be used
     */
    private void send(String exchange, Object content, String routing) {
        try {
            // first format
            String message = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(content);

            // then send
            channel.basicPublish(exchange, routing, null, message.getBytes());

            // don't forget to log
            String key = (routing != null && !routing.isEmpty())? String.format("with key \"%s\"", routing): "";
            DispatchLogger.log(String.format("Message for class %s successfully dispatched to \"%s\" exchange %s", content.getClass().getSimpleName(), exchange, key));
        } catch (Exception e) {
            DispatchLogger.log(String.format("Couldn't dispatch message to RabbitMQ because of: %s", e.getMessage()));
        }
    }

    /**
     * Shut down the singleton if it's running
     */
    public static void shutDown() {
        if (singleton != null) {
            logger.trace("Shutting down RabbitMQ Publisher");
            singleton.stop();
        }
    }

    /**
     * Close connection
     */
    private void stop() {
        try {
            if (singleton != null) {
                // close the channel
                if (singleton.channel != null) {
                    singleton.channel.close();
                    singleton.channel = null;
                }

                // close the connection
                if (singleton.connection != null) {
                    singleton.connection.close();
                    singleton.connection = null;
                }

                singleton = null;
            }
        } catch (Exception ignored) {}
    }
}
