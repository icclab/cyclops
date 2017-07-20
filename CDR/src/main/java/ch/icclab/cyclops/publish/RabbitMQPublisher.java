package ch.icclab.cyclops.publish;
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
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.util.loggers.DispatchLogger;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 13/04/16
 * Description: RabbitMQ publisher
 */
public class RabbitMQPublisher {
    final static Logger logger = LogManager.getLogger(RabbitMQPublisher.class.getName());

    // this class has to be a singleton
    private static RabbitMQPublisher singleton = null;

    // exchanges for publishing
    private String dispatchExchange;
    private String broadcastExchange;

    // connection details
    private Connection connection;
    private Channel dispatchChannel;
    private Channel broadcastChannel;

    /**
     * Access RabbitMQ object
     * @return singleton instance or null
     */
    public static RabbitMQPublisher getInstance() {
        if (singleton == null) {
            RabbitMQPublisher publisher = new RabbitMQPublisher();

            if (publisher.isConnected()) {
                singleton = publisher;
            }
        }

        return singleton;
    }

    public RabbitMQPublisher() {
        PublisherCredentials credentials = Loader.getSettings().getPublisherCredentials();

        dispatchExchange = credentials.getPublisherDispatchExchange();
        broadcastExchange = credentials.getPublisherBroadcastExchange();

        establishConnection(credentials);
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    /**
     * Establish connection based on provided credentials
     */
    public void establishConnection(PublisherCredentials credentials) {
        try {
            // setup factory
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(credentials.getPublisherUsername());
            factory.setPassword(credentials.getPublisherPassword());
            factory.setHost(credentials.getPublisherHost());
            factory.setPort(credentials.getPublisherPort());
            factory.setVirtualHost(credentials.getPublisherVirtualHost());
            factory.setAutomaticRecoveryEnabled(true);

            // establish connection
            connection = factory.newConnection();
            dispatchChannel = connection.createChannel();
            broadcastChannel = connection.createChannel();

            // declare exchange to be used (we want it to be durable and based on routing key)
            dispatchChannel.exchangeDeclare(dispatchExchange, "direct", true);
            broadcastChannel.exchangeDeclare(broadcastExchange, "fanout", true);

            logger.trace(String.format("RabbitMQ Publisher connected to %s:%d", credentials.getPublisherHost(), credentials.getPublisherPort()));
            logger.trace(String.format("RabbitMQ Publisher will dispatch to \"%s\" and broadcast to \"%s\" exchanges", dispatchExchange, broadcastExchange));

        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Publisher couldn't be created: %s", e.getMessage()));

            // remove hanging connections
            stop();
        }
    }

    /**
     * Publish a message to RabbitMQ
     * @param content to be send
     * @param routing key
     */
    protected boolean publish(Object content, String routing) {
        return send(dispatchChannel, dispatchExchange, content, routing);
    }

    /**
     * Broadcast a message to RabbitMQ
     * @param content to be send
     */
    protected boolean broadcast(Object content) {
        return send(broadcastChannel, broadcastExchange, content, "");
    }

    /**
     * Send a message to RabbitMQ
     * @param chan appropriate channel
     * @param exchange to be used
     * @param content to be sent
     * @param routing to be used
     */
    private boolean send(Channel chan, String exchange, Object content, String routing) {
        try {
            // first format
            String message = new Gson().toJson(content);

            // then send
            chan.basicPublish(exchange, routing, MessageProperties.MINIMAL_PERSISTENT_BASIC, message.getBytes());

            // don't forget to log
            String key = (routing != null && !routing.isEmpty())? String.format("with key \"%s\"", routing): "";
            DispatchLogger.log(String.format("Message for class %s successfully dispatched to \"%s\" exchange %s", content.getClass().getSimpleName(), exchange, key));
            return true;
        } catch (Exception e) {
            DispatchLogger.log(String.format("Couldn't dispatch message to RabbitMQ because of: %s", e.getMessage()));
            return false;
        }
    }

    /**
     * Close connection
     */
    private void stop() {
        try {
            dispatchChannel.close();
        } catch (Exception ignored) {}
        finally {
            dispatchChannel = null;
        }

        try {
            broadcastChannel.close();
        } catch (Exception ignored) {}
        finally {
            broadcastChannel = null;
        }

        try {
            connection.close();
        } catch (Exception ignored) {}
        finally {
            connection = null;
        }
    }

    /**
     * Shut down the singleton if it's running
     */
    public static void shutDown() {
        if (singleton != null) {
            String msg = "Shutting down RabbitMQ Publisher";
            logger.trace(msg);
            DispatchLogger.log(msg);
            singleton.stop();
            singleton = null;
        }
    }
}
