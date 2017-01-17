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

package ch.icclab.cyclops.consume;

import ch.icclab.cyclops.load.model.ConsumerCredentials;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 25/01/16
 * Description: Implementation of RabbitMQ manager for consuming tasks
 */
public class RabbitMQListener {
    final static Logger logger = LogManager.getLogger(RabbitMQListener.class.getName());

    // this class has to be a singleton
    private static RabbitMQListener singleton = null;

    // credentials to be used with RabbitMQ
    private ConsumerCredentials credentials;

    // our internal list of consumer tasks
    private List<ConsumerEntry> listOfConsumers;

    // variables needed for RabbitMQ connection
    private Channel channel;
    private Connection connection;
    private Boolean running;

    /**
     * This class holds settings for consumer jobs
     */
    private class ConsumerEntry {
        private AbstractConsumer clazz;
        private String queue;
        private Consumer consumer;
        private String tag;

        public AbstractConsumer getClazz() {
            return clazz;
        }
        public void setClazz(AbstractConsumer clazz) {
            this.clazz = clazz;
        }
        public String getQueue() {
            return queue;
        }
        public void setQueue(String queue) {
            this.queue = queue;
        }
        public Consumer getConsumer() {
            return consumer;
        }
        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }
        public String getTag() {
            return tag;
        }
        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Constructor has to be hidden
     */
    private RabbitMQListener(ConsumerCredentials cred) {
        credentials = cred;
        listOfConsumers = new ArrayList<>();
        running = false;
    }

    /**
     * Access RabbitMQ object
     * @return singleton instance
     */
    public static RabbitMQListener getInstance() { return singleton; }

    /**
     * Create RabbitMQ Consumer instance
     * @param cred credentials
     * @return instance or null
     */
    public static RabbitMQListener createInstance(ConsumerCredentials cred){
        if (singleton == null) {
            RabbitMQListener rabbitMQ = new RabbitMQListener(cred);

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

        // create channel and return status
        return ((channel = getChannel()) != null);
    }

    /**
     * Start consuming
     */
    public void start() {
        // make sure we can even start it
        if (connection != null && channel != null && !running) {
            // iterate over saved consumers
            for (ConsumerEntry entry: listOfConsumers) {

                logger.trace(String.format("RabbitMQ Consumer is listening on queue \"%s\" with consumer \"%s\"", entry.getQueue(), entry.getClazz().getClass().getSimpleName()));

                // attach consumers
                Consumer con = entry.getClazz().handleDelivery(channel);
                entry.setConsumer(con);

                // start listening
                try {
                    String tag = channel.basicConsume(entry.getQueue(), true, entry.getConsumer());
                    entry.setTag(tag);
                } catch (Exception e) {
                    logger.error("Couldn't start consuming for queue: " + entry.getQueue() + "because of: " + e.getMessage());

                    // jump to next entry
                    continue;
                }

                // everything is OK, we are running
                running = true;
            }
        }
    }

    /**
     * Stop consuming
     */
    public void stop() {
        try {
            if (singleton != null) {
                // shut down channel
                if (channel != null) {
                    for (ConsumerEntry entry: listOfConsumers) {
                        channel.basicCancel(entry.getTag());
                        channel.close();

                        channel = null;
                    }
                }

                // close connection
                if (connection != null) {
                    connection.close();
                    connection = null;
                }

                running = false;
            }
        } catch (Exception ignored) {}
    }

    /**
     * Add consumer for RabbitMQ
     * @param queue name
     * @param clazz consumer
     * @return status
     */
    public Boolean addConsumer(String queue, AbstractConsumer clazz) {

        // declare necessary queues
        try {
            channel.queueDeclare(queue, true, false, false, null);
        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Consumer couldn't declare queue named \"%s\" because of: %s", queue, e.getMessage()));
            return false;
        }

        ConsumerEntry entry = new ConsumerEntry();

        entry.setClazz(clazz);
        entry.setQueue(queue);

        listOfConsumers.add(entry);

        return true;
    }

    /**
     * Will return channel for RabbitMQ connection
     * @return channel reference or null
     */
    private Channel getChannel() {
        // connect to the RabbitMQ based on settings from Load
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(credentials.getConsumerUsername());
        factory.setPassword(credentials.getConsumerPassword());
        factory.setHost(credentials.getConsumerHost());
        factory.setPort(credentials.getConsumerPort());
        factory.setVirtualHost(credentials.getConsumerVirtualHost());
        factory.setAutomaticRecoveryEnabled(true);

        Channel chan;

        try {
            // create new connection
            connection = factory.newConnection();

            // create/connect to the channel
            chan = connection.createChannel();

            logger.trace(String.format("RabbitMQ Consumer connected to %s:%d", credentials.getConsumerHost(), credentials.getConsumerPort()));

        } catch (Exception e) {
            logger.error(String.format("RabbitMQ Consumer couldn't be created: %s", e.getMessage()));
            connection = null;
            chan = null;
        }

        // return channel reference, or null
        return chan;
    }

    /**
     * Shut down the singleton if it's running
     */
    public static void shutDown() {
        if (singleton != null) {
            logger.trace("Shutting down RabbitMQ Consumer");
            singleton.stop();
        }
    }
}
