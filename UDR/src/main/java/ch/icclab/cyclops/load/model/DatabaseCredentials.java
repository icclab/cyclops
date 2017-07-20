package ch.icclab.cyclops.load.model;
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
 * Created: 27/04/16
 * Description: Hibernate configuration and credentials
 */
public class DatabaseCredentials {

    public static int DEFAULT_DATABASE_PORT = 5432;
    public static int DEFAULT_DATABASE_PAGE_LIMIT = 500;
    public static int DEFAULT_DATABASE_CONNECTIONS = 1;

    private int databasePort;
    private String databaseHost;
    private String databaseUsername;
    private String databasePassword;
    private String databaseName;
    private int databasePageLimit;
    private int databaseConnections;

    public int getDatabasePort() {
        return databasePort;
    }
    public void setDatabasePort(int databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }
    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }
    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getDatabasePageLimit() {
        return databasePageLimit;
    }
    public void setDatabasePageLimit(int databasePageLimit) {
        this.databasePageLimit = databasePageLimit;
    }

    public int getDatabaseConnections() {
        return databaseConnections;
    }
    public void setDatabaseConnections(int databaseConnections) {
        this.databaseConnections = databaseConnections;
    }
}
