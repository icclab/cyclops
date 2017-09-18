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

import ch.icclab.cyclops.consume.command.CommandConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.ConsumerCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 02.02.17
 * Description: Manages Data and Command queue consumption
 */
public class ConsumeManager {
    final static Logger logger = LogManager.getLogger(ConsumeManager.class.getName());

    private static ConsumerCredentials consumerCredentials = Loader.getSettings().getConsumerCredentials();

    public static boolean startDataAndCommandProcessing() {
        // establish connection
        RabbitMQListener listener = RabbitMQListener.getInstance();

        // proceed only if connection was successful
        if (listener != null) {
            int threads = Runtime.getRuntime().availableProcessors();

            boolean command = listener.addConsumer(consumerCredentials.getConsumerCommandsQueue(), new CommandConsumer(true), 1);

            if (command) {
                logger.trace(String.format("RabbitMQ Consumers for %s and %s successfully initiated", consumerCredentials.getConsumerDataQueue(), consumerCredentials.getConsumerCommandsQueue()));
                return true;
            } else {
                stopDataAndCommandProcessing();
                return false;
            }
        }

        return false;
    }

    public static void stopDataAndCommandProcessing(){
        RabbitMQListener.shutDown();
    }
}
