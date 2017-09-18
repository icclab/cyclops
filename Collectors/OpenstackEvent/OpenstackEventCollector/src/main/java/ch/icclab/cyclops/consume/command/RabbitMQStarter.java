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

import ch.icclab.cyclops.load.Loader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMQStarter extends Command {

    final static Logger logger = LogManager.getLogger(RabbitMQStarter.class.getName());
    private Status status = new Status();

    public void checkAndDeclareBindings() {
        String message;
        try {
            // Creating the RabbitMQ connection
            Connection connection = this.connect();
            if (connection != null) {
                // If established, proceed to declare the queues and the bindings
                Channel channel = connection.createChannel();
                // Declaring and Binding Nova queues
                String queue = Loader.getSettings().getConsumerCredentials().getConsumerNovaQueue();
                String exchange = Loader.getSettings().getConsumerCredentials().getConsumerExchangeToBindNova();
                String routingKey = Loader.getSettings().getConsumerCredentials().getConsumerNovaRoutingKey();
                channel.queueDeclare(queue, true, false, false, null);
                channel.queueBind(queue, exchange, routingKey);

                // Declaring and Binding Neutron queues
                queue = Loader.getSettings().getConsumerCredentials().getConsumerNeutronQueue();
                exchange = Loader.getSettings().getConsumerCredentials().getConsumerExchangeToBindNeutron();
                routingKey = Loader.getSettings().getConsumerCredentials().getConsumerNeutronRoutingKey();
                channel.queueDeclare(queue, true, false, false, null);
                channel.queueBind(queue, exchange, routingKey);

                // Declaring and binding Cinder queues
                queue = Loader.getSettings().getConsumerCredentials().getConsumerCinderQueue();
                exchange = Loader.getSettings().getConsumerCredentials().getConsumerExchangeToBindCinder();
                routingKey = Loader.getSettings().getConsumerCredentials().getConsumerCinderRoutingKey();
                channel.queueDeclare(queue, true, false, false, null);
                channel.queueBind(queue, exchange, routingKey);
                channel.close();

                // Set the initiation parameter to true
                message = "All connections and bindings are successfully initiated ";
                this.status.setSuccessful(message);
            } else {
                message = "Connection is initiated but something went wrong ";
                this.status.setServerError(message);
            }
            connection.close();
        } catch (Exception e) {
            message = "Connection is not initiated: " + e.toString();
            this.status.setServerError(message);
        }
    }

    public Status getStatus(){
        return this.status;
    }

    private Connection connect() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(Loader.getSettings().getConsumerCredentials().getConsumerUsername());
            factory.setPassword(Loader.getSettings().getConsumerCredentials().getConsumerPassword());
            factory.setVirtualHost(Loader.getSettings().getConsumerCredentials().getConsumerVirtualHost());
            factory.setHost(Loader.getSettings().getConsumerCredentials().getConsumerHost());
            factory.setPort(Loader.getSettings().getConsumerCredentials().getConsumerPort());
            Connection conn = factory.newConnection();
            return conn;
        } catch (Exception e) {
            String log = "Couldn't establish RabbitMQ connection for Consumer, please check your configuration file";
            logger.error(log);
            System.err.println(log);
            this.status.setServerError(log);
        }
        return null;
    }

    @Override
    protected Status execute() {
        this.checkAndDeclareBindings();
        return this.status;
    }
}
