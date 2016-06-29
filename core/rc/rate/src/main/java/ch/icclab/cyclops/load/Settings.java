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
import org.apache.commons.lang.math.NumberUtils;
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
    protected RatingPreferences ratingPreferences;

    /**
     * Load settings based on provided settings
     */
    public Settings(Properties prop) {
        properties = prop;
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

        String routingKey = properties.getProperty("PublisherDefaultRoutingKeyIfMissing");
        if (routingKey != null && !routingKey.isEmpty()) {
            publisherCredentials.setPublisherDefaultRoutingKeyIfMissing(routingKey);
        } else {
            publisherCredentials.setPublisherDefaultRoutingKeyIfMissing(PublisherCredentials.DEFAULT_ROUTING_KEY_IF_MISSING);
        }

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

        // consumer queue name
        String consumer = properties.getProperty("ConsumerDataQueue");
        if (consumer != null && !consumer.isEmpty()) {
            consumerCredentials.setConsumerDataQueue(consumer);
        } else {
            consumerCredentials.setConsumerDataQueue(ConsumerCredentials.DEFAULT_DATA_QUEUE);
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

    //=============== Static Rating
    /**
     * Load Static Rating preferences
     * @return preferences
     */
    private RatingPreferences loadRatingPreferences() {
        RatingPreferences ratingPreferences = new RatingPreferences();

        String usage = properties.getProperty("UsageField");
        if (usage == null || usage.isEmpty()) {
            usage = RatingPreferences.DEFAULT_USAGE_FIELD;
        }
        ratingPreferences.setUsageField(usage);

        String charge = properties.getProperty("ChargeField");
        if (charge == null || charge.isEmpty()) {
            charge = RatingPreferences.DEFAULT_CHARGE_FIELD;
        }
        ratingPreferences.setChargeField(charge);

        String chargeSuffix = properties.getProperty("ChargeSuffix");
        if (chargeSuffix == null || chargeSuffix.isEmpty()) {
            chargeSuffix = RatingPreferences.DEFAULT_CHARGE_SUFFIX;
        }
        ratingPreferences.setChargeSuffix(chargeSuffix);

        String rate = properties.getProperty("RateField");
        if (rate == null || rate.isEmpty()) {
            rate = RatingPreferences.DEFAULT_RATE_FIELD;
        }
        ratingPreferences.setRateField(rate);

        ratingPreferences.setDefaultRate(NumberUtils.toDouble(properties.getProperty("DefaultRate"), RatingPreferences.DEFAULT_RATE_VALUE));

        return ratingPreferences;
    }

    /**
     * Access loaded Rating Preferences
     * @return cached preferences
     */
    public RatingPreferences getRatingPreferences() {

        if (ratingPreferences == null) {
            try {
                ratingPreferences = loadRatingPreferences();
            } catch (Exception e) {
                logger.error("Could not load Rating preferences from configuration file: " + e.getMessage());
            }
        }

        return ratingPreferences;
    }

    /**
     * Get all properties
     * @return properties
     */
    public Properties getProperties() {
        return properties;
    }
}
