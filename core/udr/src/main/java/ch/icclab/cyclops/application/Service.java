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

package ch.icclab.cyclops.application;

import ch.icclab.cyclops.endpoint.*;
import ch.icclab.cyclops.util.APICallCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Author: Skoviera
 * Created: 21/01/16
 * Description: Application class that acts as router to service endpoints
 */
public class Service {

    final static Logger logger = LogManager.getLogger(Service.class.getName());

    // Router for registering api endpoints
    private Router router;

    // API counter
    private APICallCounter counter;

    /**
     * Attaching endpoints to router
     */
    private void attachTheRest() {
        logger.trace("Attaching measurement endpoint");
        router.attach(String.format("%s/{%s}", MeasurementEndpoint.ENDPOINT, MeasurementEndpoint.ATTRIBUTE), MeasurementEndpoint.class);
        counter.registerEndpoint(MeasurementEndpoint.ENDPOINT);

        // list of measurements
        router.attach(MeasurementsEndpoint.ENDPOINT, MeasurementsEndpoint.class);
        counter.registerEndpoint(MeasurementsEndpoint.ENDPOINT);

    }

    /**
     * Construct application by accessing context, creating router and counter
     */
    private void initialiseApplication() {
        logger.trace("Initialising UDR microservice");

        router = new Router();
        counter = APICallCounter.getInstance();
    }

    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     */
    public Restlet createInboundRoot() throws Exception {

        // let's start by initialising and loading configuration settings
        initialiseApplication();

        logger.trace("Creating routes for UDR microservice");

        // root, status and list endpoints
        router.attach(RootEndpoint.ENDPOINT, RootEndpoint.class);
        counter.registerEndpoint(RootEndpoint.ENDPOINT);

        router.attach(StatusEndpoint.ENDPOINT, StatusEndpoint.class);
        counter.registerEndpointWithoutCounting(StatusEndpoint.ENDPOINT);

        router.attach(ListEndpoint.ENDPOINT, ListEndpoint.class);
        counter.registerEndpointWithoutCounting(ListEndpoint.ENDPOINT);

        // attach other endpoints
        attachTheRest();

        logger.trace("Routes for UDR microservice successfully created");

        return router;
    }
}
