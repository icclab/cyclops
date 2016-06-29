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

import ch.icclab.cyclops.util.loggers.DispatchLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 13/04/16
 * Description: Wrapper around RabbitMQ
 */
public class Messenger {
    final static Logger logger = LogManager.getLogger(Messenger.class.getName());

    // this class has to be a singleton
    private static Messenger singleton = new Messenger();

    // link to RabbitMQ
    private RabbitMQPublisher rabbitMQ;

    // container for RESTful objects
    private List<Object> container = new ArrayList<>();

    /**
     * Constructor has to be hidden
     */
    private Messenger() {
        rabbitMQ = RabbitMQPublisher.getInstance();
    }

    /**
     * Access Messenger object
     * @return singleton instance
     */
    public static Messenger getInstance() { return singleton; }

    /**
     * Publish message using RabbitMQ
     * @param content of the message
     * @param routing key
     */
    public void publish(Object content, String routing) {
        if (rabbitMQ != null) {
            rabbitMQ.publish(content, routing);
        } else {
            DispatchLogger.log("RabbitMQ is not properly configured, therefore it is not possible to publish any messages. Please consult logs and double check configuration files");
        }
    }

    /**
     * Broadcast message using RabbitM!
     * @param content of the message
     */
    public void broadcast(Object content) {
        if (rabbitMQ != null) {
            rabbitMQ.broadcast(content);
        } else {
            DispatchLogger.log("RabbitMQ is not properly configured, therefore it is not possible to publish any messages. Please consult logs and double check configuration files");
        }
    }

    /**
     * Add an object to container
     * @param obj content
     */
    public void restful(Object obj) {
        container.add(obj);
    }

    /**
     * Retrieve content from the container and empty it
     * @return container's content
     */
    public List<Object> retrieveRestfulContainer() {
        // perform a deep copy
        List<Object> ret = new ArrayList<>(container);

        // clear the container
        container.clear();

        // return the original content
        return ret;
    }
}
