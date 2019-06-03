package ch.icclab.cyclops.health;
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
import ch.icclab.cyclops.consume.RabbitMQListener;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 10.02.17
 * Description: Health check for the micro service
 */
public class HealthCheck implements Runnable {
    final static Logger logger = LogManager.getLogger(HealthCheck.class.getName());

    @Override
    public void run() {
        try {
            // test database connection
            HibernateClient.getInstance().ping();
            logger.trace("Health check: PostgreSQL is alive");

            // test whether we have live TCP connection to RabbitMQ listener
            if (RabbitMQListener.getInstance().isConnected()) logger.trace("Health check: RabbitMQ Listener is alive");
            else throw new Exception("RabbitMQ Listener is down");

            // test our TCP connection to RabbitMQ Publisher
            if (RabbitMQPublisher.getInstance().isConnected()) logger.trace("Health check: RabbitMQ Publisher is alive");
            else throw new Exception("RabbitMQ Publisher is down");

            // we passed all the tests
            HealthStatus.getInstance().setHealthy();

        } catch (Exception e) {
            logger.trace(String.format("Health check: failed (%s)", e.getMessage()));

            // we couldn't access the InfluxDB
            HealthStatus.getInstance().setUnhealthy(e.getMessage());
        }
    }
}
