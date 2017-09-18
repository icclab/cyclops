package ch.icclab.cyclops.load;
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

import ch.icclab.cyclops.load.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 21/01/16
 * Description: Parent for specific environmental settings
 */
public class Settings {

    final static Logger logger = LogManager.getLogger(Settings.class.getName());

    // Object for reading and accessing configuration properties
    private Properties properties;

    // List of different settings that are being loaded from configuration file
    protected PublisherCredentials publisherCredentials;
    protected ConsumerCredentials consumerCredentials;
    protected ServerSettings serverSettings;
    protected DatabaseCredentials databaseCredentials;
    protected OpenstackSettings openstackSettings;
    protected S3serverCredentials s3serverCredentials;


    /**
     * Load settings based on provided settings
     */
    public Settings(Properties prop) {
        properties = prop;
    }

    //=============== Restlet Server Settings
    /**
     * Load Publisher (RabbitMQ) credentials
     * @return credentials
     */
    private ServerSettings loadServerSettings() {
        ServerSettings serverSettings = new ServerSettings();

        try {
            serverSettings.setServerHTTPPort(Integer.parseInt(properties.getProperty("ServerHTTPPort")));
        } catch (Exception e) {
            serverSettings.setServerHTTPPort(-1);
        }

        try {
            serverSettings.setServerHealthCheck(Integer.parseInt(properties.getProperty("ServerHealthCheck")));
        } catch (Exception e) {
            serverSettings.setServerHealthCheck(ServerSettings.DEFAULT_SERVER_HEALTH_CHECK);
        }

        try {
            serverSettings.setServerHealthShutdown(Boolean.parseBoolean(properties.getProperty("ServerHealthShutdown")));
        } catch (Exception e) {
            serverSettings.setServerHealthShutdown(false);
        }

        return serverSettings;
    }

    /**
     * Access loaded Server Settings
     * @return server settings
     */
    public ServerSettings getServerSettings() {

        if (serverSettings == null) {
            serverSettings = loadServerSettings();
        }

        return serverSettings;
    }

    //=============== Openstack Collector Settings
    /**
     * Load Openstack Collector Settings
     * @return first import date
     */
    private OpenstackSettings loadOpenstackSettings() {
        OpenstackSettings openstackSettings = new OpenstackSettings();


        openstackSettings.setOpenstackCollectorEventRun(properties.getProperty("OpenstackCollectorEventRun"));
        openstackSettings.setOpenstackCollectorEventPause( properties.getProperty("OpenstackCollectorEventPause"));
        openstackSettings.setOpenstackCollectorEventStop( properties.getProperty("OpenstackCollectorEventStop"));
        openstackSettings.setOpenstackCollectorEventDelete( properties.getProperty("OpenstackCollectorEventDelete"));
        openstackSettings.setOpenstackCollectorEventSuspend( properties.getProperty("OpenstackCollectorEventSuspend"));
        openstackSettings.setOpenstackDefaultRegion(properties.getProperty("OpenstackDefaultRegion"));

        return openstackSettings;
    }

    /**
     * Access loaded Openstack Settings
     * @return Openstack settings
     */
    public OpenstackSettings getOpenstackSettings() {

        if (openstackSettings == null) {
            openstackSettings = loadOpenstackSettings();
        }

        return openstackSettings;
    }

    /**
     * Load S3 server credentials
     * @return first import date
     */
    private S3serverCredentials loadS3serverCredentials() {
        S3serverCredentials s3serverCredentials = new S3serverCredentials();

        s3serverCredentials.setS3KeyId(properties.getProperty("S3KeyId"));
        s3serverCredentials.setS3Key(properties.getProperty("S3Key"));
        s3serverCredentials.setEndpoint( properties.getProperty("Endpoint"));

        return s3serverCredentials;
    }

    /**
     * Access loaded S3 credentials
     * @return S3 credentials
     */
    public S3serverCredentials getS3serverCredentials(){
        if (s3serverCredentials == null){
            s3serverCredentials = loadS3serverCredentials();
        }

        return  s3serverCredentials;
    }

    //=============== Database Credentials
    /**
     * Load Database credentials
     * @return credentials
     */
    private DatabaseCredentials loadDatabaseCredentials() {
        DatabaseCredentials databaseCredentials = new DatabaseCredentials();

        try {
            databaseCredentials.setDatabasePort(Integer.parseInt(properties.getProperty("DatabasePort")));
        } catch (Exception e) {
            databaseCredentials.setDatabasePort(DatabaseCredentials.DEFAULT_DATABASE_PORT);
        }

        try {
            databaseCredentials.setDatabasePageLimit(Integer.parseInt(properties.getProperty("DatabasePageLimit")));
        } catch (Exception e) {
            databaseCredentials.setDatabasePageLimit(DatabaseCredentials.DEFAULT_DATABASE_PAGE_LIMIT);
        }

        try {
            databaseCredentials.setDatabaseConnections(Integer.parseInt(properties.getProperty("DatabaseConnections")));
        } catch (Exception e) {
            databaseCredentials.setDatabaseConnections(DatabaseCredentials.DEFAULT_DATABASE_CONNECTIONS);
        }

        databaseCredentials.setDatabaseHost(properties.getProperty("DatabaseHost"));
        databaseCredentials.setDatabaseUsername(properties.getProperty("DatabaseUsername"));
        databaseCredentials.setDatabasePassword(properties.getProperty("DatabasePassword"));
        databaseCredentials.setDatabaseName(properties.getProperty("DatabaseName"));

        return databaseCredentials;
    }

    /**
     * Access loaded Database Credentials
     * @return server settings
     */
    public DatabaseCredentials getDatabaseCredentials() {

        if (databaseCredentials == null) {
            databaseCredentials = loadDatabaseCredentials();
        }

        return databaseCredentials;
    }

    //=============== RabbitMQ Publisher
    /**
     * Load Publisher (RabbitMQ) credentials
     * @return credentials
     */
    private PublisherCredentials loadPublisherCredentials() {
        PublisherCredentials publisherCredentials = new PublisherCredentials();

        publisherCredentials.setPublisherHost(properties.getProperty("PublisherHost"));
        publisherCredentials.setPublisherUsername(properties.getProperty("PublisherUsername"));
        publisherCredentials.setPublisherPassword(properties.getProperty("PublisherPassword"));
        publisherCredentials.setPublisherPort(Integer.parseInt(properties.getProperty("PublisherPort")));
        publisherCredentials.setPublisherVirtualHost(properties.getProperty("PublisherVirtualHost"));

        // publisher dispatch exchange name
        String dispatch = properties.getProperty("PublisherDispatchExchange");
        if (dispatch != null && !dispatch.isEmpty()) {
            publisherCredentials.setPublisherDispatchExchange(dispatch);
        } else {
            publisherCredentials.setPublisherDispatchExchange(PublisherCredentials.DEFAULT_PUBLISHER_DISPATCH_EXCHANGE);
        }

        // publisher dispatch exchange name
        String broadcast = properties.getProperty("PublisherBroadcastExchange");
        if (broadcast != null && !broadcast.isEmpty()) {
            publisherCredentials.setPublisherBroadcastExchange(broadcast);
        } else {
            publisherCredentials.setPublisherBroadcastExchange(PublisherCredentials.DEFAULT_PUBLISHER_BROADCAST_EXCHANGE);
        }

        return publisherCredentials;
    }

    /**
     * Access loaded Publisher (RabbitMQ) credentials
     * @return cached credentials
     */
    public PublisherCredentials getPublisherCredentials() {

        if (publisherCredentials == null) {
            try {
                publisherCredentials = loadPublisherCredentials();
            } catch (Exception e) {
                logger.error("Could not load Publisher (RabbitMQ) credentials from configuration file: " + e.getMessage());
            }
        }

        return publisherCredentials;
    }

    //=============== RabbitMQ Consumer
    /**
     * Load Consumer (RabbitMQ) credentials
     * @return credentials
     */
    private ConsumerCredentials loadConsumerCredentials() {
        ConsumerCredentials consumerCredentials = new ConsumerCredentials();

        consumerCredentials.setConsumerHost(properties.getProperty("ConsumerHost"));
        consumerCredentials.setConsumerUsername(properties.getProperty("ConsumerUsername"));
        consumerCredentials.setConsumerPassword(properties.getProperty("ConsumerPassword"));
        consumerCredentials.setConsumerPort(Integer.parseInt(properties.getProperty("ConsumerPort")));
        consumerCredentials.setConsumerVirtualHost(properties.getProperty("ConsumerVirtualHost"));

        // commands queue name
        String commands = properties.getProperty("ConsumerCommandsQueue");
        if (commands != null && !commands.isEmpty()) {
            consumerCredentials.setConsumerCommandsQueue(commands);
        } else {
            consumerCredentials.setConsumerCommandsQueue(ConsumerCredentials.DEFAULT_COMMANDS_QUEUE);
        }

        return consumerCredentials;
    }

    /**
     * Access loaded Consumer (RabbitMQ) credentials
     * @return cached credentials
     */
    public ConsumerCredentials getConsumerCredentials() {

        if (consumerCredentials == null) {
            try {
                consumerCredentials = loadConsumerCredentials();
            } catch (Exception e) {
                logger.error("Could not load Consumer (RabbitMQ) credentials from configuration file: " + e.getMessage());
            }
        }

        return consumerCredentials;
    }
}
