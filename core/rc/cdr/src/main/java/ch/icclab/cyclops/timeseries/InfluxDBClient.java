/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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

package ch.icclab.cyclops.timeseries;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.util.loggers.TimeSeriesLogger;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atteo.evo.inflector.English;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: Client class for InfluxDB
 */
public class InfluxDBClient {
    final static Logger logger = LogManager.getLogger(InfluxDBClient.class.getName());

    // singleton
    private InfluxDBCredentials credentials;
    private InfluxDB session;

    /**
     * Constructor
     * @param conf to be used
     */
    public InfluxDBClient(InfluxDBCredentials conf) {
        credentials = conf;
        session = obtainSession();
    }

    public InfluxDBClient() {
        credentials = Loader.getSettings().getInfluxDBCredentials();
        session = obtainSession();
    }

    /**
     * Ask for connection to InfluxDB
     * @return session
     */
    private InfluxDB obtainSession() {
        return InfluxDBFactory.connect(credentials.getInfluxDBURL(), credentials.getInfluxDBUsername(), credentials.getInfluxDBPassword());
    }

    /**
     * Ping InfluxDB server to see whether it is alive
     * @throws Exception
     */
    public void ping() throws Exception {
        session.ping();
    }

    /**
     * Enable batch processing for items that are added as single points
     * @param flushPoints flush every X points (for example 2000)
     * @param flushFrequency flush every Y time unit (for example 100 ms)
     * @param unit time unit (for example milliseconds)
     */
    public void configureBatchOnSinglePoints(Integer flushPoints, Integer flushFrequency, TimeUnit unit) {
        session.enableBatch(flushPoints, flushFrequency, unit);
    }

    /**
     * Save container to InfluxDB
     * @param container to be persisted
     */
    public void persistContainer(BatchPointsContainer container) {
        persistContainer(container.getPoints());
    }
    private void persistContainer(BatchPoints container) {
        TimeSeriesLogger.log(String.format("Saving container with %d points to database", container.getPoints().size()));
        session.write(container);
    }

    /**
     * Request BatchPoint container
     * @return container
     */
    protected BatchPoints getEmptyContainer() {
        return BatchPoints.database(credentials.getInfluxDBTSDB()).build();
    }

    /**
     * Persist single point
     * @param builder to generate point
     */
    public void persistSinglePoint(Point.Builder builder) {
        // add mandatory hidden field
        Point point = builder.addField(InfluxDBCredentials.COUNTER_FIELD_NAME, true).build();

        // depending on whether batch processing for single points is enabled store immediately or wait for flush
        session.write(credentials.getInfluxDBTSDB(), "default", point);
    }

    /**
     * Create databases based on list of names
     * @param names for database creation
     */
    public void createDatabases(String ... names) {
        // now create required databases
        for (String name: names) {
            TimeSeriesLogger.log(String.format("Making sure \"%s\" database exists", name));
            session.createDatabase(name);
        }
    }

    /**
     * Execute query
     * @param builder QueryBuilder
     * @return InfluxDBResponse or null
     */
    public InfluxDBResponse executeQuery(QueryBuilder builder) {
        return executeQuery(Collections.singletonList(builder));
    }
    public InfluxDBResponse executeQuery(List<QueryBuilder> builders) {
        try {
            TimeSeriesLogger.log(String.format("About to execute %d %s", builders.size(), English.plural("query", builders.size())));

            // concatenate multiple queries into one
            List<String> queries = builders.stream().map(QueryBuilder::build).collect(Collectors.toList());
            String multipleQuery = StringUtils.join(queries, ";");

            // connect to InfluxDB and execute query
            QueryResult result = session.query(new Query(multipleQuery, credentials.getInfluxDBTSDB()));

            // return InfluxDB response or null
            return (!result.hasError())? new InfluxDBResponse(result): null;

        } catch (Exception ignored) {
            TimeSeriesLogger.log(String.format("Query execution failed: %s", ignored.getMessage()));
            return null;
        }
    }
}
