package ch.icclab.cyclops.application;

import ch.icclab.cyclops.endpoint.RootEndpoint;
import ch.icclab.cyclops.endpoint.SchedulerEndpoint;
import ch.icclab.cyclops.endpoint.StatusEndpoint;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.util.APICallCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 30/05/16.
 */

public class Service {
    final static Logger logger = LogManager.getLogger(Service.class.getName());

    // Router for registering api endpoints
    private Router router;

    // API counter
    private APICallCounter counter;

    // Settings
    private Settings settings;

    /**
     * Construct application by accessing context, creating router and counter
     */
    private void initialiseApplication() {
        logger.trace("Initialising OpenStack collector microservice");

        router = new Router();
        counter = APICallCounter.getInstance();

        // application settings
        settings = Loader.getSettings();
    }


    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     */
    public Restlet createInboundRoot() {

        // let's start by initialising and loading configuration settings
        initialiseApplication();

        logger.trace("Creating routes for OpenStack collector microservice");

        // root, status and list endpoints
        router.attach(RootEndpoint.ENDPOINT, RootEndpoint.class);
        counter.registerEndpoint(RootEndpoint.ENDPOINT);

        router.attach(StatusEndpoint.ENDPOINT, StatusEndpoint.class);
        counter.registerEndpointWithoutCounting(StatusEndpoint.ENDPOINT);

        router.attach(SchedulerEndpoint.ENDPOINT, SchedulerEndpoint.class);
        counter.registerEndpoint(SchedulerEndpoint.ENDPOINT);


        logger.trace("Routes for OpenStack collector microservice successfully created");

        return router;
    }
}
