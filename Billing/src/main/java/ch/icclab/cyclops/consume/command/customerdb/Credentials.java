package ch.icclab.cyclops.consume.command.customerdb;
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

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 02.08.17
 * Description: Customer database credentials
 */
public class Credentials {
    private String CustomerDatabaseHost;
    private String CustomerDatabasePort;

    public Credentials() {
    }

    public String getCustomerDatabaseHost() {
        return CustomerDatabaseHost;
    }
    public void setCustomerDatabaseHost(String customerDatabaseHost) {
        CustomerDatabaseHost = customerDatabaseHost;
    }

    public String getCustomerDatabasePort() {
        return CustomerDatabasePort;
    }
    public void setCustomerDatabasePort(String customerDatabasePort) {
        CustomerDatabasePort = customerDatabasePort;
    }

    public boolean isValid() {
        return (CustomerDatabaseHost != null && !CustomerDatabaseHost.isEmpty() && CustomerDatabasePort != null && !getCustomerDatabasePort().isEmpty());
    }
}
