package ch.icclab.cyclops.consume.command;
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

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.ConsumerEntry;
import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.health.HealthCheck;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.util.loggers.CommandLogger;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 14/04/16
 * Description: Commands consumer
 */
public class CommandConsumer extends AbstractConsumer {
    private static String INVALID_RECORDS_ROUTING_KEY = "invalid_command";
    private Command.Status status;
    private boolean healthCheck;

    public CommandConsumer() {
        status = new Command.Status();
        healthCheck = false;
    }

    public CommandConsumer(boolean check) {
        status = new Command.Status();
        healthCheck = check;
    }

    @Override
    public void consume(String content, ConsumerEntry consumer, Long deliveryTag) {

        // automatic mapping based on type field
        Command command = CommandMapping.fromJson(content);

        // execute the command
        if (command != null) {
            status = command.execute();
            CommandLogger.log(String.format("%s - %s", command.getCommand(), status.toString()));
        } else {
            status.setClientError("Unknown command or invalid JSON");
            CommandLogger.log(String.format("%s", status.toString()));
        }

        // message came from RabbitMQ
        if (deliveryTag != null) {
            // everything went OK, command executed correctly, ACK the message
            if (status.hasSucceeded()) consumer.ackMessage(deliveryTag);

            // there was a server error, NACK the message and optionally schedule health check
            else if (status.hadServerError()) {
                consumer.nackMessage(deliveryTag);
                if (healthCheck) TaskExecutor.getInstance().executeNow(new HealthCheck());
            }

            // there was a client error, ACK the message and publish it via exchange for invalid commands
            else if (status.hadClientError()) {
                boolean status = Messenger.publish(content, INVALID_RECORDS_ROUTING_KEY);
                if (status) consumer.ackMessage(deliveryTag);
                // publish failed, nack the message and optionally schedule health check
                else {
                    consumer.nackMessage(deliveryTag);
                    if (healthCheck) TaskExecutor.getInstance().executeNow(new HealthCheck());
                }
            }

            // not so desired state, apparently command developer forgot to set the execution status
            else consumer.ackMessage(deliveryTag);
        }
    }

    public Command.Status getStatus() {
        return status;
    }
}
