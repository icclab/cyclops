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
import ch.icclab.cyclops.util.loggers.TimeSeriesLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 25.04.17
 * Description: Database pool implementation
 */
class DbPool {

    final static Logger logger = LogManager.getLogger(DbPool.class.getName());

    private static HikariDataSource pool;

    /**
     * Get connection from shared pool
     * @return connection or null in case of error
     */
    static Connection getConnection() {
        Connection connection = null;

        // first make sure pool is initialized
        if (pool == null) {
            try {
                pool = initialize();
                TimeSeriesLogger.log("Database pool initialized");
            } catch (Exception e) {
                pool = null;
                TimeSeriesLogger.log("Database pool couldn't be initialized");
            }
        }

        // then get a new connection
        if (pool != null) {
            try {
                connection = pool.getConnection();
            } catch (Exception e) {
                connection = null;
            }
        }

        return connection;
    }

    /**
     * Initialize and create new Data Source
     * @return data source
     */
    private static HikariDataSource initialize() throws Exception {
        DatabaseCredentials credentials = Loader.getSettings().getDatabaseCredentials();
        HikariConfig config = DBPoolConf.getHikariConfig(credentials);
        return new HikariDataSource(config);
    }

    /**
     * Shut down the Pool manager
     */
    static void shutDown() {
        if (pool != null) {
            String msg = "Shutting down Database Pool manager";
            logger.trace(msg);
            TimeSeriesLogger.log(msg);
            pool.close();
            pool = null;
        }
    }
}
