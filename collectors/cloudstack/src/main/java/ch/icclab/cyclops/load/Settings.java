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
package ch.icclab.cyclops.load;

import ch.icclab.cyclops.load.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Author: Skoviera
 * Created: 21/01/16
 * Description: Parent for specific environmental settings
 */
public class Settings {

    final static Logger logger = LogManager.getLogger(Settings.class.getName());

    // Object for reading and accessing configuration properties
    private Properties properties;

    // List of different settings that are being loaded from configuration file
    protected PublisherCredentials publisherCredentials;
    protected HibernateCredentials hibernateCredentials;
    protected CloudStackSettings cloudStackSettings;
    protected ServerSettings serverSettings;

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

    //============== CloudStack settings
    private CloudStackSettings loadCloudStackSettings() {
        CloudStackSettings cloudstack = new CloudStackSettings(properties.getProperty("CloudStackURL"), properties.getProperty("CloudStackAPIKey"),
                properties.getProperty("CloudStackSecretKey"), properties.getProperty("CloudStackPageSize"));

        // not mandatory, but helpful date of the first import
        String date = properties.getProperty("CloudStackFirstImport");
        if (date != null && !date.isEmpty()) {
            cloudstack.setCloudStackImportFrom(date);
        }

        return cloudstack;
    }

    public CloudStackSettings getCloudStackSettings() {
        if (cloudStackSettings == null) {
            try {
                cloudStackSettings = loadCloudStackSettings();
            } catch (Exception e) {
                logger.error("Could not load CloudStack Settings from configuration file: " + e.getMessage());
            }
        }

        return cloudStackSettings;
    }
}
