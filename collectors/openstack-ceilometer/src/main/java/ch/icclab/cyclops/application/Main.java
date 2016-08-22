package ch.icclab.cyclops.application;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.load.model.HibernateCredentials;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.HibernateConfiguration;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.schedule.runner.OpenStackCeilometerClient;
import ch.icclab.cyclops.util.ShutDownListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.restlet.Component;
import org.restlet.data.Protocol;

import java.util.concurrent.TimeUnit;

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

public class Main {
    final static Logger logger = LogManager.getLogger(Main.class.getName());

    private static final int OK_HELP = 1;
    private static final int ERR_PARAMS = 2;
    private static final int ERR_SETTINGS = 3;
    private static final int ERR_HIBERNATE_CON = 4;
    private static final int ERR_HIBERNATE_LOAD = 5;
    private static final int ERR_ROUTER = 6;
    private static final int ERR_START = 7;
    private static final int ERR_RABBITMQ = 8;

    private static final Boolean EMPTY_LINE = true;
    private static final Boolean NO_EMPTY_LINE = false;

    private static final String helpMessage = String.join(System.getProperty("line.separator"),
            "RCB Cyclops: OpenStack collector micro service",
            "Author: Manu Perez, ICCLab ZHAW", "",
            "Required parameters: path to configuration file",
            "Optional parameters: HTTP port", "",
            "Example: java -jar openstack-collector.jar config.txt 4567");

    public static void main(String[] args) {
        outputProgressBar("Loading RCB Cyclops OpenStack collector micro service ");

        // check params and potentially stop execution
        checkParameters(args);

        // check help parameter and potentially output it
        String param = args[0];
        checkHelp(param);

        // check configuration file and make sure it's correct
        checkConfigurationFile(param);

        // check whether second parameter is port number
        checkCustomPortOption(args);

        // check and configure hibernate
        checkAndConfigureHibernate();

        // check and configure rabbitMQ
        checkAndSetupRabbitMQ();

        // and finally create and start the server
        Component component = new Component();

        // check and prepare ports
        bindServer(component);

        // everything is ready, start the server
        checkAndStartServer(component);
    }

    /**
     * Check number of parameters
     *
     * @param args as string array
     */
    private static void checkParameters(String[] args) {
        if (args.length == 0) {
            String log = "A configuration file path has to be provided (as argument), otherwise OpenStack collector micro service cannot be properly loaded";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_PARAMS);
        }

        outputProgressBar();
    }

    /**
     * Check whether parameter was help
     *
     * @param param to be examined
     */
    private static void checkHelp(String param) {
        if (param.equals("-h") || param.equals("--help")) {
            System.out.println(helpMessage);
            System.exit(OK_HELP);
        }

        outputProgressBar();
    }

    /**
     * Make sure configuration file is valid
     *
     * @param param path
     */
    private static void checkConfigurationFile(String param) {
        try {
            // create and parse configuration settings
            Loader.createInstance(param);
        } catch (Exception e) {
            String log = "The configuration file is corrupted, make sure it's according to documentation and all fields are specified";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_SETTINGS);
        }

        outputProgressBar();
    }

    /**
     * Check and configure Hibernate
     */
    private static void checkAndConfigureHibernate() {
        try {
            // get credentials
            HibernateCredentials credentials = Loader.getSettings().getHibernateCredentials();

            // create configuration
            Configuration configuration = HibernateConfiguration.createConfiguration(credentials);

            // create Hibernate
            HibernateClient.createInstance(configuration);

        } catch (Exception e) {
            String log = String.format("Couldn't connect to Hibernate: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_HIBERNATE_CON);
        }

        outputProgressBar();
    }

    /**
     * Connect to RabbitMQ with Publisher and Consumer
     */
    private static void checkAndSetupRabbitMQ() {

        try {
            Settings settings = Loader.getSettings();

            // prepare settings
            if (settings != null) {

                // prepare Publisher RabbitMQ connection
                PublisherCredentials credentials = settings.getPublisherCredentials();
                RabbitMQPublisher publisher = RabbitMQPublisher.createInstance(credentials);

                if (publisher != null) {
                    logger.trace("RabbitMQ Publisher successfully initialised");
                } else {
                    String error = "RabbitMQ Publisher won't be used, as RabbitMQ credentials are missing or not valid";
                    logger.error(error);

                    throw new Exception(error);
                }
                //This collector does not consume from any RabbitMQ, so it only has Publisher
            } else {
                String error = "Configuration file is missing or corrupted, thus RabbitMQ cannot be used";
                logger.error(error);
                throw new Exception(error);
            }
        } catch (Exception e) {
            String log = "Couldn't establish RabbitMQ connection for Publisher and Consumer, please check your configuration file";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_RABBITMQ);
        }

        outputProgressBar();
    }


    /**
     * Make sure ports are valid
     *
     * @param component to be bind
     */
    private static void bindServer(Component component) {
        // access settings and specified ports
        logger.trace("Loading specified configuration file and server settings");

        Integer HTTPPort = Loader.getSettings().getServerSettings().getPort();


        try {
            component.getServers().add(Protocol.HTTP, HTTPPort);
            logger.trace(String.format("Binding HTTP to port %d", HTTPPort));
        } catch (Exception e) {
            logger.trace(String.format("Was not possible to bind HTTP to port %d", HTTPPort));
        }

        logger.trace("Setting up OpenStack collector micro service");

        try {
            component.getDefaultHost().attach(new Service().createInboundRoot());
        } catch (org.hibernate.HibernateException e) {
            String log = String.format("Couldn't load data from backend database via Hibernate: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_HIBERNATE_LOAD);
        } catch (Exception e) {
            String log = String.format("Couldn't create router and initialise things accordingly: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_ROUTER);
        }

        outputProgressBar();
    }

    /**
     * Check port specification
     *
     * @param args to be processed
     */
    private static void checkCustomPortOption(String[] args) {
        if (args.length > 1) {
            try {
                Integer port = Integer.parseInt(args[1]);
                Loader.getSettings().getServerSettings().setPort(port);

                logger.trace(String.format("Custom port specified as %d, configuration file will be ignored", port));
            } catch (Exception ignored) {
            }
        }

        outputProgressBar();
    }

    private static void outputProgressBar() {
        outputProgressBar("...");
    }

    private static void outputProgressBar(Boolean... emptyLine) {
        outputProgressBar("...", emptyLine);
    }

    private static void outputProgressBar(String text, Boolean... emptyLine) {
        if (emptyLine.length > 0 && emptyLine[0]) {
            System.out.println();
        }

        System.out.print(text);

        if (emptyLine.length > 1 && emptyLine[1]) {
            System.out.println();
        }
    }

    private static void checkAndStartServer(Component component) {
        logger.trace("Starting the OpenStack Collector micro service");
        try {
            // register a shutdown listener
            Runtime.getRuntime().addShutdownHook(new ShutDownListener());
            // also start collection immediately
            Scheduler scheduler = Scheduler.getInstance();
            int frequency = Loader.getSettings().getSchedulerSettings().getFrequency();
            scheduler.addRunner(new OpenStackCeilometerClient(), 0, frequency, TimeUnit.SECONDS);
            scheduler.start();
            outputProgressBar(NO_EMPTY_LINE, EMPTY_LINE);

            // and finally start the server
            component.getClients().add(Protocol.HTTP);
            component.start();
        } catch (Exception e) {
            String log = String.format("Couldn't start the server: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_START);
        }
    }
}
