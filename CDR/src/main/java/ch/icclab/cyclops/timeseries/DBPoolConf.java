package ch.icclab.cyclops.timeseries;
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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;

import java.util.Properties;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 25.04.17
 * Description: Hikari pool configuration
 */
class DBPoolConf {
    static HikariConfig getHikariConfig(DatabaseCredentials c) {
        Properties props = new Properties();

        String URL = String.format("jdbc:postgresql://%s:%d/%s?stringtype=unspecified",
                c.getDatabaseHost(), c.getDatabasePort(), c.getDatabaseName());

        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.url", URL);
        props.setProperty("dataSource.serverName", c.getDatabaseHost());
        props.setProperty("dataSource.portNumber", Integer.toString(c.getDatabasePort()));
        props.setProperty("dataSource.user", c.getDatabaseUsername());
        props.setProperty("dataSource.password", c.getDatabasePassword());
        props.setProperty("dataSource.databaseName", c.getDatabaseName());

        // this should be at least 30 seconds lower than underlying database
        props.setProperty("maxLifetime", "240000");

        // set based on concurrency preference (corresponding to data ingestion in RabbitMQ)
        props.setProperty("maximumPoolSize", String.format("%d", Loader.getSettings().getDatabaseCredentials().getDatabaseConnections()));

        return new HikariConfig(props);
    }
}
