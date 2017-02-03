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

import ch.icclab.cyclops.consume.RabbitMQListener;
import ch.icclab.cyclops.consume.data.consumer.CinderConsumer;
import ch.icclab.cyclops.consume.data.consumer.NeutronConsumer;
import ch.icclab.cyclops.consume.data.consumer.NovaConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.HibernateCredentials;
import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.HibernateConfiguration;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import ch.icclab.cyclops.schedule.runner.openstack.CinderUDRRunner;
import ch.icclab.cyclops.schedule.runner.openstack.NeutronUDRRunner;
import ch.icclab.cyclops.schedule.runner.openstack.NovaUDRRunner;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.util.ShutDownListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.restlet.Application;
import org.restlet.Component;

import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 26/04/16
 * Description: Entry point for Openstack event collector micro service
 */
public class Main extends Application{

    static {
        // Nothing can appear before this initializer
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
    }

    final static Logger logger = LogManager.getLogger(Main.class.getName());

    private static final int OK_HELP = 1;
    private static final int ERR_PARAMS = 2;
    private static final int ERR_SETTINGS = 3;
    private static final int ERR_INFLUXDB = 4;
    private static final int ERR_RABBITMQ = 5;
    private static final int ERR_HIBERNATE_CON = 6;
    private static final int ERR_START = 7;

    private static final Boolean EMPTY_LINE = true;
    private static final Boolean NO_EMPTY_LINE = false;

    private static final String helpMessage = String.join(System.getProperty("line.separator"),
            "RCB Cyclops: Openstack event collector micro service",
            "Author: Martin Skoviera, ICCLab ZHAW", "",
            "Required parameters: path to configuration file",
            "Optional parameters: HTTP port","",
            "Example: java -jar openstack-event-collector.jar config.txt 4567");

    public static void main(String[] args){
        outputProgressBar("Loading RCB Cyclops Openstack event collector micro service ");

        // check params and potentially stop execution
        checkParameters(args);

        // check help parameter and potentially output it
        String param = args[0];
        checkHelp(param);

        // check configuration file and make sure it's correct
        checkConfigurationFile(param);


        // check and configure InfluxDB time series database
        checkAndConfigureInfluxDB();

        // check and setup RabbitMQ publisher and consumer
        checkAndSetupRabbitMQ();

        // check and configure hibernate
        checkAndConfigureHibernate();

        // and finally create and start the server
        Component component = new Component();

        // everything is ready, start the server
        checkAndStartServer(component);
    }

    /**
     * Check number of parameters
     * @param args as string array
     */
    private static void checkParameters(String[] args) {
        if (args.length == 0) {
            String log = "A configuration file path has to be provided (as argument), otherwise Openstack event collector micro service cannot be properly loaded";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_PARAMS);
        }

        outputProgressBar();
    }

    /**
     * Check whether parameter was help
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

                // prepare Consumer RabbitMQ connection
                RabbitMQListener consumer = RabbitMQListener.createInstance(settings.getConsumerCredentials());
                if (consumer != null) {

                    // bind consumers
                    Boolean conNova = consumer.addConsumer(settings.getConsumerCredentials().getConsumerNovaQueue(), new NovaConsumer());
                    Boolean conCinder = consumer.addConsumer(settings.getConsumerCredentials().getConsumerCinderQueue(), new CinderConsumer());
                    Boolean conNeutron = consumer.addConsumer(settings.getConsumerCredentials().getConsumerNeutronQueue(), new NeutronConsumer());

                    if (conNova && conCinder && conNeutron) {
                        logger.trace("RabbitMQ Consumer successfully initialised");
                        consumer.start();
                    } else {
                        String error = "RabbitMQ Consumer couldn't connect to specified queues, check your configuration";
                        logger.error(error);
                        throw new Exception(error);
                    }
                } else {
                    String error = "RabbitMQ Consumer won't be used, as RabbitMQ credentials are missing or not valid";
                    logger.error(error);
                    throw new Exception(error);
                }

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
     * Check and configure InfluxDB
     */
    private static void checkAndConfigureInfluxDB() {
        InfluxDBClient influxDBClient = new InfluxDBClient();
        try {
            logger.trace("Binding to InfluxDB and creating databases");
            InfluxDBCredentials credentials = Loader.getSettings().getInfluxDBCredentials();
            InfluxDBClient client = new InfluxDBClient(credentials);

            client.ping();
            client.createDatabases(credentials.getInfluxDBTSDB());
        } catch (Exception e) {
            String log = String.format("Couldn't connect to InfluxDb: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_INFLUXDB);
        }
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

    private static void checkAndStartServer(Component component) {
        logger.trace("Starting the Openstack event collector micro service");
        try {
            // register a shutdown listener
            Runtime.getRuntime().addShutdownHook(new ShutDownListener());

            outputProgressBar(NO_EMPTY_LINE, EMPTY_LINE);

            // also start collection immediately
            Long time = new Long(Loader.getSettings().getOpenstackSettings().getOpenstackScheduleTime());
            Scheduler scheduler = Scheduler.getInstance();
            scheduler.addRunner(new NovaUDRRunner(), 0, time, TimeUnit.MILLISECONDS);
            scheduler.addRunner(new NeutronUDRRunner(), 0, time, TimeUnit.MILLISECONDS);
            scheduler.addRunner(new CinderUDRRunner(), 0, time, TimeUnit.MILLISECONDS);
            scheduler.start();

            // and finally start the server
            component.start();
        } catch (Exception e) {
            String log = String.format("Couldn't start the server: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_START);
        }
    }

    private static void outputProgressBar() {
        outputProgressBar("...");
    }

    private static void outputProgressBar(Boolean ... emptyLine) {
        outputProgressBar("...", emptyLine);
    }

    private static void outputProgressBar(String text, Boolean ... emptyLine) {
        if (emptyLine.length > 0 && emptyLine[0]) {
            System.out.println();
        }

        System.out.print(text);

        if (emptyLine.length > 1 && emptyLine[1]) {
            System.out.println();
        }
    }
}
