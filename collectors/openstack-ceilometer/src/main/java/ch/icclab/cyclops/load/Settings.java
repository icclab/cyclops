package ch.icclab.cyclops.load;

import ch.icclab.cyclops.load.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

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

public class Settings {

    final static Logger logger = LogManager.getLogger(Settings.class.getName());

    private Properties properties;

    protected OpenStackSettings openStackSettings;
    protected ServerSettings serverSettings;
    protected PublisherCredentials publisherCredentials;
    protected HibernateCredentials hibernateCredentials;
    protected SchedulerSettings schedulerSettings;

    //Load all the settings based on the provided settings
    public Settings(Properties properties) {
        this.properties = properties;
    }

    protected ServerSettings loadServerSettings() {
        logger.debug("Attempting to load the server settings.");
        ServerSettings serverSettings = new ServerSettings();

        try {
            serverSettings.setPort(Integer.parseInt(properties.getProperty("ServerHTTPPort")));
            serverSettings.setDemo(properties.getProperty("Demo"));
            serverSettings.setPageSize(Integer.parseInt(properties.getProperty("PageSize")));
            logger.debug("Server settings loaded.");
        } catch (Exception e) {
            logger.error("Error while loading the server settings.");
            serverSettings.setPort(-1);
        }

        return serverSettings;
    }

    protected OpenStackSettings loadOpenStackSettings() {
        OpenStackSettings openStackSettings = new OpenStackSettings();
        try {
            openStackSettings.setCeilometerUrl(properties.getProperty("CeilometerUrl"));
            openStackSettings.setAccount(properties.getProperty("OpenstackAccount"));
            openStackSettings.setPassword(properties.getProperty("OpenstackPassword"));
            openStackSettings.setKeystoneDomain(properties.getProperty("KeystoneDomain"));
            openStackSettings.setKeystoneTenant(properties.getProperty("KeystoneTenant"));
            openStackSettings.setKeystoneUrl(properties.getProperty("KeystoneUrl"));
            openStackSettings.setMeterUrl(properties.getProperty("MetersUrl"));
            openStackSettings.setSupportedMeterList(properties.getProperty("SupportedMeterList"));
            logger.debug("OpenStack settings loaded.");
        } catch (Exception e) {
            logger.error("Error while loading the OpenStack settings.");
        }
        return openStackSettings;
    }

    protected PublisherCredentials loadPublisherCredentials(){
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

    public HibernateCredentials loadHibernateCredentials(){
        HibernateCredentials hibernateCredentials = new HibernateCredentials();

        hibernateCredentials.setHibernateURL(properties.getProperty("HibernateURL"));
        hibernateCredentials.setHibernateUsername(properties.getProperty("HibernateUsername"));
        hibernateCredentials.setHibernatePassword(properties.getProperty("HibernatePassword"));
        hibernateCredentials.setHibernateDriver(properties.getProperty("HibernateDriver"));
        hibernateCredentials.setHibernateDialect(properties.getProperty("HibernateDialect"));

        return hibernateCredentials;
    }

    public SchedulerSettings loadSchedulerSettings(){
        SchedulerSettings schedulerSettings = new SchedulerSettings();

        schedulerSettings.setFrequency(Integer.parseInt(properties.getProperty("SchedulerFrequency")));

        return schedulerSettings;
    }

    public HibernateCredentials getHibernateCredentials(){
        if(hibernateCredentials == null)
            hibernateCredentials = loadHibernateCredentials();
        return hibernateCredentials;
    }

    public OpenStackSettings getOpenStackSettings() {
        if (openStackSettings == null)
            openStackSettings = loadOpenStackSettings();
        return openStackSettings;
    }

    public ServerSettings getServerSettings() {
        if (serverSettings == null)
            serverSettings = loadServerSettings();
        return serverSettings;
    }

    public PublisherCredentials getPublisherCredentials(){
        if (publisherCredentials == null)
            publisherCredentials = loadPublisherCredentials();
        return publisherCredentials;
    }

    public SchedulerSettings getSchedulerSettings(){
        if (schedulerSettings == null)
            schedulerSettings = loadSchedulerSettings();
        return schedulerSettings;
    }
}
