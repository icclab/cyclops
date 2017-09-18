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
    protected HibernateCredentials hibernateCredentials;

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
            serverSettings.setServerHTTPSPort(Integer.parseInt(properties.getProperty("ServerHTTPSPort")));
        } catch (Exception e) {
            serverSettings.setServerHTTPSPort(-1);
        }

        try {
            serverSettings.setServerHTTPSCertPath(properties.getProperty("ServerHTTPSCertPath"));
        } catch (Exception e) {
            serverSettings.setServerHTTPSCertPath(null);
        }

        try {
            serverSettings.setServerHTTPSPassword(properties.getProperty("ServerHTTPSPassword"));
        } catch (Exception e) {
            serverSettings.setServerHTTPSPassword(null);
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

    //=============== Hibernate credentials and settings
    /**
     * Load Hibernate credentials
     * @return credentials
     */
    private HibernateCredentials loadHibernateConfiguration() {
        HibernateCredentials hibernateCredentials = new HibernateCredentials();

        hibernateCredentials.setHibernateURL(properties.getProperty("HibernateURL"));
        hibernateCredentials.setHibernateUsername(properties.getProperty("HibernateUsername"));
        hibernateCredentials.setHibernatePassword(properties.getProperty("HibernatePassword"));
        hibernateCredentials.setHibernateDriver(properties.getProperty("HibernateDriver"));
        hibernateCredentials.setHibernateDialect(properties.getProperty("HibernateDialect"));

        return hibernateCredentials;
    }

    /**
     * Access loaded Hibernate credentials
     * @return server settings
     */
    public HibernateCredentials getHibernateCredentials() {

        if (hibernateCredentials == null) {
            hibernateCredentials = loadHibernateConfiguration();
        }

        return hibernateCredentials;
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
        publisherCredentials.setPublisherDispatchExchange(properties.getProperty("PublisherDispatchExchange"));
        publisherCredentials.setPublisherBroadcastExchange(properties.getProperty("PublisherBroadcastExchange"));

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
        consumerCredentials.setConsumeFromQueue(properties.getProperty("ConsumeFromQueue"));

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
