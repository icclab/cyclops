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

import ch.icclab.cyclops.consume.ConsumeManager;
import ch.icclab.cyclops.health.HealthStatus;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.ServerSettings;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.ShutDownListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import java.util.TimeZone;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 26/04/16
 * Description: Entry point for Billing micro service
 */
public class Main extends Application{

    static {
        // Nothing can appear before this initializer
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    final static Logger logger = LogManager.getLogger(Main.class.getName());

    private static final int OK_HELP = 1;
    private static final int ERR_PARAMS = 2;
    private static final int ERR_SETTINGS = 3;
    private static final int ERR_DATABASE = 4;
    private static final int ERR_RABBITMQ = 5;
    private static final int ERR_ROUTER = 6;
    public static final int ERR_HEALTH = 7;
    private static final int ERR_START = 8;

    private static final Boolean EMPTY_LINE = true;
    private static final Boolean NO_EMPTY_LINE = false;

    private static final String helpMessage = String.join(System.getProperty("line.separator"),
            "RCB Cyclops: Billing micro service",
            "Author: Martin Skoviera, cyclops-labs.io", "",
            "Required parameters: path to configuration file",
            "Optional parameters: HTTP port","",
            "Example: java -jar billing.jar config.txt 4567");

    public static void main(String[] args){
        outputProgressBar("Loading RCB Cyclops Billing micro service ");

        // check params and potentially stop execution
        checkParameters(args);

        // check help parameter and potentially output it
        String param = args[0];
        checkHelp(param);

        // check configuration file and make sure it's correct
        checkConfigurationFile(param);

        // check whether second parameter is port number
        checkCustomPortOption(args);

        // check and configure InfluxDB time series database
        checkAndConfigureDatabase();

        // check and setup RabbitMQ publisher and consumer
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
     * @param args as string array
     */
    private static void checkParameters(String[] args) {
        if (args.length == 0) {
            String log = "A configuration file path has to be provided (as argument), otherwise Billing micro service cannot be properly loaded";
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
        // establish connections with RabbitMQ exchanges and queues
        if (RabbitMQPublisher.getInstance() == null) {
            String log = "Couldn't establish connection with RabbitMQ Publisher";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_RABBITMQ);
        }

        if (!ConsumeManager.startDataAndCommandProcessing()) {
            String log = "Couldn't establish connection with RabbitMQ Consumer";
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_RABBITMQ);
        }

        outputProgressBar();
    }

    /**
     * Make sure ports are valid
     * @param component to be bind
     */
    private static void bindServer(Component component) {
        // access settings and specified ports
        logger.trace("Loading specified configuration file and server settings");

        ServerSettings settings = Loader.getSettings().getServerSettings();

        Integer HTTPPort = settings.getServerHTTPPort();
        if (HTTPPort <= 0) logger.trace(String.format("Skipping HTTP interface creation as the port is %d", HTTPPort));
        else {
            logger.trace(String.format("Going to bind HTTP interface to port %d", HTTPPort));
            component.getServers().add(Protocol.HTTP, HTTPPort);
        }

        Integer HTTPSPort = settings.getServerHTTPSPort();

        if (HTTPSPort <= 0) logger.trace(String.format("Skipping HTTPS interface creation as the port is %d", HTTPSPort));
        else {
            logger.trace(String.format("Going to bind HTTPS interface to port %d", HTTPSPort));

            String certPath = settings.getServerHTTPSCertPath();
            String password = settings.getServerHTTPSPassword();

            if (certPath == null || certPath.isEmpty()) logger.trace("Skipping HTTPS interface creation as cert is missing");
            else if (password == null || password.isEmpty()) logger.trace("Skipping HTTPS interface creation as cert password is missing");
            else {
                logger.trace(String.format("Cert located at %s", certPath));

                Server server = component.getServers().add(Protocol.HTTPS, HTTPSPort);
                Series parameters = server.getContext().getParameters();
                parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
                parameters.add("keyStorePath", certPath);
                parameters.add("keyStorePassword", password);
                parameters.add("keyPassword", password);
                parameters.add("keyStoreType", "PKCS12");
            }
        }

        try {
            component.getDefaultHost().attach(new Service().createInboundRoot());
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
     * @param args to be processed
     */
    private static void checkCustomPortOption(String[] args) {
        if (args.length > 1) {
            try {
                Integer port = Integer.parseInt(args[1]);
                Loader.getSettings().getServerSettings().setServerHTTPPort(port);

                logger.trace(String.format("Custom port specified as %d, configuration file will be ignored", port));
            } catch (Exception ignored){}
        }

        outputProgressBar();
    }

    /**
     * Check and configure InfluxDB
     */
    private static void checkAndConfigureDatabase() {
        try {
            logger.trace("Binding to database");

            // check database connection
            if (!new DbAccess().ping()) throw new Exception("check the configuration file");

        } catch (Exception e) {
            String log = String.format("Couldn't connect to database: %s", e.getMessage());
            logger.error(log);
            System.err.println(log);
            System.exit(ERR_DATABASE);
        }

        outputProgressBar();
    }

    private static void checkAndStartServer(Component component) {
        logger.trace("Starting the Billing micro service");
        try {
            // register a shutdown listener
            Runtime.getRuntime().addShutdownHook(new ShutDownListener());

            outputProgressBar(NO_EMPTY_LINE, EMPTY_LINE);

            // add health check
            HealthStatus.getInstance().scheduleCheck();

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
