package ch.icclab.cyclops.util;
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

import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import ch.icclab.cyclops.schedule.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Skoviera
 * Created: 11/03/16
 * Description: Shut down micro service mercifully
 */
public class ShutDownListener extends Thread{

    final static Logger logger = LogManager.getLogger(ShutDownListener.class.getName());

    @Override
    public void run() {
        logger.trace("We are shutting down CloudStack collector micro service");

        // don't forget about Publisher (RabbitMQ)
        RabbitMQPublisher.shutDown();

        // let's also kill scheduler
        Scheduler.getInstance().stop();

        // do the same with Hibernate
        HibernateClient.shutDown();
    }
}
