package ch.icclab.cyclops.application;
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

import ch.icclab.cyclops.endpoint.*;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.rule.RuleException;
import ch.icclab.cyclops.rule.RuleManagement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 21/01/16
 * Description: Application class that acts as router to service endpoints
 */
public class RuleEngine {

    final static Logger logger = LogManager.getLogger(RuleEngine.class.getName());

    // Router for registering api endpoints
    private Router router;

    // Settings
    private Settings settings;

    /**
     * Initialise inference engine by loading rule instances from DB to memory
     */
    private void setUpDrools() throws RuleException, DatabaseException {

        // now do the same with facts
        logger.trace("Loading Facts and Rules from DB and inserting them into Working and Production memory");

        // get access to Drools
        RuleManagement ruleManagement = RuleManagement.getInstance();
        HibernateClient hibernate = HibernateClient.getInstance();

        // initialise it all
        ruleManagement.initialiseWorkingMemory(hibernate);
    }

    /**
     * Implement here your own endpoints you want to expose and track
     */
    private void attachCustomEndpoints() {
        // attach endpoints to router and track them
        logger.trace("Attaching template endpoint");
        router.attach("/template/{id}", TemplateEndpoint.class);
        router.attach("/template", TemplateEndpoint.class);
        router.attach("/templates", TemplateEndpoint.class);

        logger.trace("Attaching instance endpoint");
        router.attach("/rule/{id}", RuleEndpoint.class);
        router.attach("/rule", RuleEndpoint.class);
        router.attach("/rules", RuleEndpoint.class);

        logger.trace("Attaching fact endpoint");
        router.attach("/fact", FactEndpoint.class);
        router.attach("/facts", FactEndpoint.class);

        logger.trace("Attaching newrule endpoint");
        router.attach("/newrule", NewRuleEndpoint.class);

        logger.trace("Attaching status endpoint");
        router.attach("/status", StatusEndpoint.class);
    }

    /**
     * Construct application by accessing context, creating router and counter
     */
    private void initialiseApplication() {
        logger.trace("Initialising Rule Engine microservice");

        router = new Router();
        // application settings
        settings = Loader.getSettings();
    }

    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     */
    public Restlet createInboundRoot() throws RuleException, DatabaseException {

        // let's start by initialising and loading configuration settings
        initialiseApplication();

        logger.trace("Creating routes for Rule Engine microservice");

        // following endpoints are available
        router.attach("/", RootEndpoint.class);

        // attach custom endpoints
        attachCustomEndpoints();

        // initialise drools working memory
        setUpDrools();

        logger.trace("Routes for Rule Engine microservice successfully created");

        return router;
    }
}
