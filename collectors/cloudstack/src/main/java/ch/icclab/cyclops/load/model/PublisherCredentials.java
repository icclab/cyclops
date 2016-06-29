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
package ch.icclab.cyclops.load.model;

/**
 * Author: Skoviera
 * Created: 25/01/16
 * Description: Publisher (RabbitMQ) credentials
 */
public class PublisherCredentials {

    public static String DEFAULT_PUBLISHER_DISPATCH_EXCHANGE = "cyclops.cloudstack_collector.dispatch";
    public static String DEFAULT_PUBLISHER_BROADCAST_EXCHANGE = "cyclops.cloudstack_collector.broadcast";

    // These fields correspond with the configuration file
    private String publisherUsername;
    private String publisherPassword;
    private String publisherHost;
    private int publisherPort;
    private String publisherVirtualHost;

    private String publisherDispatchExchange;
    private String publisherBroadcastExchange;

    //==== Getters and Setters
    public String getPublisherUsername() {
        return publisherUsername;
    }
    public void setPublisherUsername(String publisherUsername) {
        this.publisherUsername = publisherUsername;
    }

    public String getPublisherPassword() {
        return publisherPassword;
    }
    public void setPublisherPassword(String publisherPassword) {
        this.publisherPassword = publisherPassword;
    }

    public String getPublisherHost() {
        return publisherHost;
    }
    public void setPublisherHost(String publisherHost) {
        this.publisherHost = publisherHost;
    }

    public int getPublisherPort() {
        return publisherPort;
    }
    public void setPublisherPort(int publisherPort) {
        this.publisherPort = publisherPort;
    }

    public String getPublisherVirtualHost() {
        return publisherVirtualHost;
    }
    public void setPublisherVirtualHost(String publisherVirtualHost) {
        this.publisherVirtualHost = publisherVirtualHost;
    }

    public String getPublisherDispatchExchange() {
        return publisherDispatchExchange;
    }
    public void setPublisherDispatchExchange(String publisherDispatchExchange) {
        this.publisherDispatchExchange = publisherDispatchExchange;
    }

    public String getPublisherBroadcastExchange() {
        return publisherBroadcastExchange;
    }
    public void setPublisherBroadcastExchange(String publisherBroadcastExchange) {
        this.publisherBroadcastExchange = publisherBroadcastExchange;
    }
}
