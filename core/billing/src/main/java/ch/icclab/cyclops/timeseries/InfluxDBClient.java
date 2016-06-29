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

import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.util.loggers.TimeSeriesLogger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: Client class for InfluxDB
 */
public class InfluxDBClient {
    final static Logger logger = LogManager.getLogger(InfluxDBClient.class.getName());
    public static String MEASUREMENT_FIELD_NAME = "name";

    // singleton
    private static InfluxDBClient singleton;
    private InfluxDBCredentials credentials;

    /**
     * Create InfluxDB instance
     * @param conf to be used
     */
    public static void createInstance(InfluxDBCredentials conf){
        if (singleton == null) {
            singleton = new InfluxDBClient(conf);
        }
    }

    /**
     * Constructor
     * @param conf to be used
     */
    private InfluxDBClient(InfluxDBCredentials conf) {
        credentials = conf;

        createDatabases(credentials.getInfluxDBTSDB());
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of InfluxDB object
     */
    public static InfluxDBClient getInstance() {
        return singleton;
    }

    /**
     * Ask for connection to InfluxDB
     * @return session
     */
    private InfluxDB obtainSession() {
        return InfluxDBFactory.connect(credentials.getInfluxDBURL(), credentials.getInfluxDBUsername(), credentials.getInfluxDBPassword());
    }

    /**
     * Save container to InfluxDB
     * @param container to be persisted
     */
    public void persistContainer(BatchPointsContainer container) {
        persistContainer(container.getPoints());
    }
    private void persistContainer(BatchPoints container) {
        InfluxDB con = obtainSession();
        TimeSeriesLogger.log(String.format("Saving container with %d points to database", container.getPoints().size()));
        con.write(container);
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
        BatchPoints container = getEmptyContainer();

        // add mandatory hidden field
        Point point = builder.addField(InfluxDBCredentials.COUNTER_FIELD_NAME, true).build();

        container.point(point);

        persistContainer(container);
    }

    /**
     * Create databases based on list of names
     * @param names for database creation
     */
    public void createDatabases(String ... names) {
        InfluxDB client = obtainSession();

        // now create required databases
        for (String name: names) {
            TimeSeriesLogger.log(String.format("Making sure \"%s\" database exists", name));
            client.createDatabase(name);
        }
    }

    /**
     * Execute query
     * @param builder QueryBuilder
     * @return QueryResult
     */
    public List<Map> executeQuery(QueryBuilder builder) {
        InfluxDB client = obtainSession();

        // execute query
        QueryResult result = client.query(new Query(builder.build(), credentials.getInfluxDBTSDB()));

        // return it parsed as list of maps
        return ParseQueryResult.parse(result);
    }

    /**
     * Execute query and map it to specified class
     * @param builder query
     * @param clazz for mapping
     * @return list or null
     */
    public List executeQueryAndMapItToClass(QueryBuilder builder, Class clazz) {
        // first execute query and get results
        List<Map> result = executeQuery(builder);

        // now return it parsed
        return ParseQueryResult.map(result, clazz);
    }
}
