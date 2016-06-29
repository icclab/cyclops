package ch.icclab.cyclops.application;
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

import ch.icclab.cyclops.consume.RabbitMQListener;
import ch.icclab.cyclops.consume.data.DataConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Skoviera
 * Created: 26/04/16
 * Description: Entry point for Static Rating micro service
 */
public class Main{

    final static Logger logger = LogManager.getLogger(Main.class.getName());

    private static final int OK_HELP = 1;
    private static final int ERR_PARAMS = 2;
    private static final int ERR_SETTINGS = 3;
    private static final int ERR_RABBITMQ = 4;

    private static final Boolean EMPTY_LINE = true;
    private static final Boolean NO_EMPTY_LINE = false;

    private static final String helpMessage = String.join(System.getProperty("line.separator"),
            "RCB Cyclops: Static Rating micro service",
            "Author: Martin Skoviera, ICCLab ZHAW", "",
            "Required parameters: path to configuration file",
            "Example: java -jar rate.jar config.txt");

    public static void main(String[] args){
        outputProgressBar("Loading RCB Cyclops Static Rating micro service ");

        // check params and potentially stop execution
        checkParameters(args);

        // check help parameter and potentially output it
        String param = args[0];
        checkHelp(param);

        // check configuration file and make sure it's correct
        checkConfigurationFile(param);

        // check and setup RabbitMQ publisher and consumer
        checkAndSetupRabbitMQ();
    }

    /**
     * Check number of parameters
     * @param args as string array
     */
    private static void checkParameters(String[] args) {
        if (args.length == 0) {
            String log = "A configuration file path has to be provided (as argument), otherwise Static Rating micro service cannot be properly loaded";
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
                    Boolean con = consumer.addConsumer(settings.getConsumerCredentials().getConsumerDataQueue(), new DataConsumer(credentials));
                    if (con) {
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

        outputProgressBar(NO_EMPTY_LINE, EMPTY_LINE);
        System.out.println("Ready for consuming");
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
