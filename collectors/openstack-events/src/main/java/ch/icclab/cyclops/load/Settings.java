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
    protected ConsumerCredentials consumerCredentials;
    protected InfluxDBCredentials influxDBCredentials;
    protected HibernateCredentials hibernateCredentials;
    protected OpenstackSettings openstackSettings;


    /**
     * Load settings based on provided settings
     */
    public Settings(Properties prop) {
        properties = prop;
    }


    //=============== Openstack Collector Settings
    /**
     * Load Openstack Collector Settings
     * @return first import date
     */
    private OpenstackSettings loadOpenstackSettings() {
        OpenstackSettings openstackSettings = new OpenstackSettings();


        openstackSettings.setOpenstackFirstImport(properties.getProperty("OpenstackFirstImportDate"));
        openstackSettings.setOpenstackCollectorEventRun(properties.getProperty("OpenstackCollectorEventRun"));
        openstackSettings.setOpenstackCollectorEventPause( properties.getProperty("OpenstackCollectorEventPause"));
        openstackSettings.setOpenstackCollectorEventStop( properties.getProperty("OpenstackCollectorEventStop"));
        openstackSettings.setOpenstackCollectorEventDelete( properties.getProperty("OpenstackCollectorEventDelete"));
        openstackSettings.setOpenstackCollectorEventSuspend( properties.getProperty("OpenstackCollectorEventSuspend"));
        openstackSettings.setOpenstackScheduleTime(properties.getProperty("OpenstackCollectorScheduleTime"));

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

        // should publisher also include unknown classes?
        publisherCredentials.setPublisherIncludeAlsoUnknown(Boolean.parseBoolean(properties.getProperty("PublisherIncludeAlsoUnknown")));

        // should publisher broadcast instead of route?
        publisherCredentials.setPublisherByDefaultDispatchInsteadOfBroadcast(Boolean.parseBoolean(properties.getProperty("PublisherByDefaultDispatchInsteadOfBroadcast")));

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

        consumerCredentials.setConsumerNovaQueue(properties.getProperty("ConsumerNovaQueue"));
        consumerCredentials.setConsumerCinderQueue(properties.getProperty("ConsumerCinderQueue"));
        consumerCredentials.setConsumerNeutronQueue(properties.getProperty("ConsumerNeutronQueue"));



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

    //=============== InfluxDB credentials
    /**
     * Load InfluxDB credentials
     * @return credentials
     */
    private InfluxDBCredentials loadInfluxDBCredentials() {
        InfluxDBCredentials influxDBCredentials= new InfluxDBCredentials();

        // URL address
        influxDBCredentials.setInfluxDBURL(String.format("http://%s:%s", properties.getProperty("InfluxDBHost"), properties.getProperty("InfluxDBPort")));
        influxDBCredentials.setInfluxDBUsername(properties.getProperty("InfluxDBUsername"));
        influxDBCredentials.setInfluxDBPassword(properties.getProperty("InfluxDBPassword"));

        // database name
        String dbName = properties.getProperty("InfluxDBTSDB");
        if (dbName != null && !dbName.isEmpty()) {
            influxDBCredentials.setInfluxDBTSDB(dbName);
        } else {
            influxDBCredentials.setInfluxDBTSDB(InfluxDBCredentials.DEFAULT_DATABASE_NAME);
        }

        // measurement name
        String defaultMeasurement = properties.getProperty("InfluxDBDefaultMeasurement");
        if (defaultMeasurement != null && !defaultMeasurement.isEmpty()) {
            influxDBCredentials.setInfluxDBDefaultMeasurement(defaultMeasurement);
        } else {
            influxDBCredentials.setInfluxDBDefaultMeasurement(InfluxDBCredentials.DEFAULT_MEASUREMENT_NAME);
        }

        // page limit
        Integer limit = InfluxDBCredentials.DEFAULT_PAGE_SIZE_LIMIT;
        try {
            // load it from configuration file
            Integer size = Integer.parseInt(properties.getProperty("InfluxDBPageSizeLimit"));

            // only override if page limit makes sense
            if (size > 0) {
                limit = size;
            }
        } catch (Exception ignored) {
        } finally {
            // set appropriate page limit
            influxDBCredentials.setInfluxDBPageSizeLimit(limit);
        }

        // query timeout in seconds
        Integer timeout = InfluxDBCredentials.DEFAULT_QUERY_TIMEOUT;
        try {
            // load it from configuration file
            Integer time = Integer.parseInt(properties.getProperty("InfluxDBQueryTimeout"));

            // only override if page limit makes sense
            if (time > 0) {
                timeout = time;
            }
        } catch (Exception ignored) {
        } finally {
            // set appropriate timeout
            influxDBCredentials.setInfluxDBQueryTimeout(timeout);
        }

        return influxDBCredentials;
    }

    /**
     * Access loaded InfluxDB credentials
     * @return cached credentials
     */
    public InfluxDBCredentials getInfluxDBCredentials() {

        if (influxDBCredentials == null) {
            try {
                influxDBCredentials = loadInfluxDBCredentials();
            } catch (Exception e) {
                logger.error("Could not load InfluxDB credentials from configuration file: " + e.getMessage());
            }
        }

        return influxDBCredentials;
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
     * @return hibernate credentials
     */
    public HibernateCredentials getHibernateCredentials() {

        if (hibernateCredentials == null) {
            hibernateCredentials = loadHibernateConfiguration();
        }

        return hibernateCredentials;
    }


}
