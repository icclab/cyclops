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

import ch.icclab.cyclops.util.loggers.DispatchLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 13/04/16
 * Description: Wrapper around RabbitMQ
 */
public class Messenger {
    /**
     * Publish message using RabbitMQ
     * @param content of the message
     * @param routing key
     */
    public static boolean publish(Object content, String routing) {
        RabbitMQPublisher rabbitMQ = RabbitMQPublisher.getInstance();

        if (rabbitMQ != null) {
            return rabbitMQ.publish(content, routing);
        } else {
            DispatchLogger.log("RabbitMQ is not properly configured, therefore it is not possible to publish any messages. Please consult logs and double check configuration files");
            return false;
        }
    }

    /**
     * Broadcast message using RabbitMQ
     * @param content of the message
     */
    public static boolean broadcast(Object content) {
        RabbitMQPublisher rabbitMQ = RabbitMQPublisher.getInstance();

        if (rabbitMQ != null) {
            return rabbitMQ.broadcast(content);
        } else {
            DispatchLogger.log("RabbitMQ is not properly configured, therefore it is not possible to publish any messages. Please consult logs and double check configuration files");
            return false;
        }
    }
}
