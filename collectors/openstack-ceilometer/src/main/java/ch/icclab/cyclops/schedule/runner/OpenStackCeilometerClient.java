/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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
package ch.icclab.cyclops.schedule.runner;

import ch.icclab.cyclops.client.OpenStackPuller;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenStackSettings;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Author: Manu Perez
 * Created on: 22-June-16
 * Description: Client class for Telemetry. Asks underlying classes for OpenStack data and saves it
 */
public class OpenStackCeilometerClient extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(OpenStackCeilometerClient.class.getName());

    // will be used as object to pull data from OpenStack
    private static OpenStackPuller openStackPuller;

    // connection to Load
    private static OpenStackSettings settings;


    /**
     * Simple constructor that will create OpenStack Puller object
     */
    public OpenStackCeilometerClient() {
        openStackPuller = new OpenStackPuller();
        settings = Loader.getSettings().getOpenStackSettings();
    }

    /**
     * This method gets called from outside in order to get data from OpenStack
     */
    private void updateRecords() {
        logger.debug("Started with updating Usage Records from OpenStack");
        HibernateClient hibernateClient = HibernateClient.getInstance();

        // get data from Ceilometer
        Boolean status = openStackPuller.pullUsageRecords();

        if (!status) {
            logger.error("Couldn't update OpenStack Usage data");
        }else{
            // get now
            Long time = new DateTime(DateTimeZone.UTC).getMillis();

            // update time stamp
            LatestPullORM pull = (LatestPullORM) hibernateClient.getObject(LatestPullORM.class, 1l);
            if (pull == null) {
                pull = new LatestPullORM(time);
            } else {
                pull.setTimeStamp(time);
            }

            hibernateClient.persistObject(pull);
        }
    }

    @Override
    public void run() {
        updateRecords();
    }
}
