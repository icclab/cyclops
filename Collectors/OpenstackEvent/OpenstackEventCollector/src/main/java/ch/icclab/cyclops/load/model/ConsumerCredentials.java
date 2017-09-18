package ch.icclab.cyclops.load.model;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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

/**
 * Author: Skoviera
 * Created: 25/01/16
 * Description: Consumer (RabbitMQ) credentials
 */
public class ConsumerCredentials {
    public static String DEFAULT_COMMANDS_QUEUE = "cyclops.openstack.event.commands";


    // These fields correspond with the configuration file
    private String consumerUsername;
    private String consumerPassword;
    private String consumerHost;
    private int consumerPort;
    private String consumerVirtualHost;
    private String consumerNovaQueue;
    private String consumerExchangeToBindNova;
    private String consumerNovaRoutingKey;
    private String consumerCinderQueue;
    private String consumerExchangeToBindCinder;
    private String consumerCinderRoutingKey;
    private String consumerNeutronQueue;
    private String consumerExchangeToBindNeutron;
    private String consumerNeutronRoutingKey;
    private String consumerCommandsQueue;


    //==== Getters and Setters
    public String getConsumerUsername() {
        return consumerUsername;
    }
    public void setConsumerUsername(String consumerUsername) {
        this.consumerUsername = consumerUsername;
    }

    public String getConsumerPassword() {
        return consumerPassword;
    }
    public void setConsumerPassword(String consumerPassword) {
        this.consumerPassword = consumerPassword;
    }

    public String getConsumerHost() {
        return consumerHost;
    }
    public void setConsumerHost(String consumerHost) {
        this.consumerHost = consumerHost;
    }

    public int getConsumerPort() {
        return consumerPort;
    }
    public void setConsumerPort(int consumerPort) {
        this.consumerPort = consumerPort;
    }

    public String getConsumerVirtualHost() {
        return consumerVirtualHost;
    }
    public void setConsumerVirtualHost(String consumerVirtualHost) {
        this.consumerVirtualHost = consumerVirtualHost;
    }

    public String getConsumerNovaQueue() {
        return consumerNovaQueue;
    }
    public void setConsumerNovaQueue(String consumerNovaQueue) {
        this.consumerNovaQueue = consumerNovaQueue;
    }

    public String getConsumerCinderQueue() {
        return consumerCinderQueue;
    }
    public void setConsumerCinderQueue(String consumerCinderQueue) {
        this.consumerCinderQueue = consumerCinderQueue;
    }

    public String getConsumerNeutronQueue() {
        return consumerNeutronQueue;
    }
    public void setConsumerNeutronQueue(String consumerNeutronQueue) {
        this.consumerNeutronQueue = consumerNeutronQueue;
    }

    public String getConsumerExchangeToBindNova() {
        return consumerExchangeToBindNova;
    }

    public void setConsumerExchangeToBindNova(String consumerExchangeToBindNova) {
        this.consumerExchangeToBindNova = consumerExchangeToBindNova;
    }

    public String getConsumerNovaRoutingKey() {
        return consumerNovaRoutingKey;
    }

    public void setConsumerNovaRoutingKey(String consumerNovaRoutingKey) {
        this.consumerNovaRoutingKey = consumerNovaRoutingKey;
    }

    public String getConsumerExchangeToBindCinder() {
        return consumerExchangeToBindCinder;
    }

    public void setConsumerExchangeToBindCinder(String consumerExchangeToBindCinder) {
        this.consumerExchangeToBindCinder = consumerExchangeToBindCinder;
    }

    public String getConsumerCinderRoutingKey() {
        return consumerCinderRoutingKey;
    }

    public void setConsumerCinderRoutingKey(String consumerCinderRoutingKey) {
        this.consumerCinderRoutingKey = consumerCinderRoutingKey;
    }

    public String getConsumerExchangeToBindNeutron() {
        return consumerExchangeToBindNeutron;
    }

    public void setConsumerExchangeToBindNeutron(String consumerExchangeToBindNeutron) {
        this.consumerExchangeToBindNeutron = consumerExchangeToBindNeutron;
    }

    public String getConsumerNeutronRoutingKey() {
        return consumerNeutronRoutingKey;
    }

    public void setConsumerNeutronRoutingKey(String consumerNeutronRoutingKey) {
        this.consumerNeutronRoutingKey = consumerNeutronRoutingKey;
    }

    public String getConsumerCommandsQueue() {
        return consumerCommandsQueue;
    }
    public void setConsumerCommandsQueue(String consumerCommandsQueue) {
        this.consumerCommandsQueue = consumerCommandsQueue;
    }
}
