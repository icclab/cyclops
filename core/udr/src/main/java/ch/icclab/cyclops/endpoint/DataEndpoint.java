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

package ch.icclab.cyclops.endpoint;

import ch.icclab.cyclops.consume.command.model.generic.model.UDR;
import ch.icclab.cyclops.consume.data.DataConsumer;
import ch.icclab.cyclops.consume.data.DataProcess;
import ch.icclab.cyclops.dto.Measurement;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.InfluxDBResponse;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import com.google.gson.Gson;
import org.apache.commons.lang.math.NumberUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 08/07/16
 * Description: Handle uploading data frames (the same way as with RabbitMQ)
 */
public class DataEndpoint extends AbstractEndpoint {

    private static String SELECTED_MEASUREMENT = UDR.class.getSimpleName();

    // used for accessing DataConsumer
    private PublisherCredentials publisher;
    private String defaultName;

    // supported functions
    private final static String FUN_COUNT = "count";

    // supported params
    private final static String PARAM_PAGE = "page";
    private final static String PARAM_FROM = "from";
    private final static String PARAM_TO = "to";

    private final InfluxDBClient dbClient = new InfluxDBClient();

    public DataEndpoint() {
        Settings settings = Loader.getSettings();
        this.defaultName = settings.getInfluxDBCredentials().getInfluxDBDefaultMeasurement();
        this.publisher = settings.getPublisherCredentials();
    }

    @Override
    public String getRoute() {
        return "/data";
    }

    @Get
    public String processGet(){

        // access query user specified
        Map params = getQuery().getValuesMap();

        // prepare measurement
        Measurement measurement = new Measurement(SELECTED_MEASUREMENT, params);

        // has user provided page number?
        Integer pageNumber = 0;
        if (params.containsKey(PARAM_PAGE)) {
            pageNumber = NumberUtils.toInt((String) params.get(PARAM_PAGE), 0);
            params.remove(PARAM_PAGE);
        }

        // prepare query
        QueryBuilder builder = prepareQuery(measurement, params);
        // get number of pages
        Integer count = getCountForMeasurement(builder);

        // set page limit
        Integer pageLimit = Loader.getSettings().getInfluxDBCredentials().getInfluxDBPageSizeLimit();
        measurement.setPageSize(pageLimit);

        // only if it makes sense
        if (count > 0) {
            // get data from InfluxDB
            fillMeasurementWithRecordsFromDatabase(measurement, builder, pageNumber, pageLimit);
            measurement.setTotalRecords(count);
        } else {
            measurement.setDisplayedRecords(0);
            measurement.setTotalRecords(0);
        }

        // don't forget to log it
        RESTLogger.log(String.format("Serving %d records (out of %d) for measurement \"%s\" as page %d", measurement.getDisplayedRecords(), measurement.getTotalRecords(), measurement.getMeasurement(), measurement.getPageNumber()));

        // return measurement
        return new Gson().toJson(measurement);
    }

    /**
     * Prepare query based on measurement and parameters
     * @param measurement details
     * @param params to be used
     * @return QueryBuilder
     */
    private QueryBuilder prepareQuery(Measurement measurement, Map<String, String> params) {
        // create query builder
        QueryBuilder builder = new QueryBuilder(measurement.getMeasurement());

        // date range specification FROM
        if (params.containsKey(PARAM_FROM)) {
            Long from = NumberUtils.toLong(params.get(PARAM_FROM), 0);

            // only if it makes sense
            if (from > 0) {
                builder.timeFrom(from, DataProcess.TIME_UNIT);
            }

            params.remove(PARAM_FROM);
        }

        // date range specification TO
        if (params.containsKey(PARAM_TO)) {
            Long to = NumberUtils.toLong(params.get(PARAM_TO), 0);

            // only if it makes sense
            if (to > 0) {
                builder.timeTo(to, DataProcess.TIME_UNIT);
            }

            params.remove(PARAM_TO);
        }

        // add where clauses iteratively
        for (Map.Entry<String, String> entry: params.entrySet()) {
            String strNumber = entry.getValue();
            // if it is number
            if (NumberUtils.isNumber(strNumber)) {
                builder.where(entry.getKey(), NumberUtils.createDouble(strNumber));
            } else {
                builder.where(entry.getKey(), entry.getValue());
            }
        }

        return builder;
    }

    /**
     * Count number of records for specified query
     * @param builder prepare query builder
     * @return Integer
     */
    private Integer getCountForMeasurement(QueryBuilder builder) {
        try {
            // add COUNT select and pass builder
            InfluxDBResponse response = dbClient.executeQuery(builder.count(InfluxDBCredentials.COUNTER_FIELD_NAME));
            List<Map> countResult = response.getListOfObjects();

            // get count from first record
            return ((Double) countResult.get(0).get(FUN_COUNT)).intValue();
        } catch (Exception ignored) {
            return 0;
        }
    }

    /**
     * Add result of database query to measurement
     * @param measurement to be filled
     * @param builder prepared query
     * @param pageNumber to be used
     * @param pageLimit for pagination
     */
    private void fillMeasurementWithRecordsFromDatabase(Measurement measurement, QueryBuilder builder, Integer pageNumber, Integer pageLimit) {

        try {
            // reset selected fields first
            builder.resetSelectClause();

            // add limit and offset, plus execute query
            InfluxDBResponse response = dbClient.executeQuery(builder.limit(pageLimit).offset(pageNumber*pageLimit));
            List<Map> parsed = response.getListOfObjects();

            // update measurement
            measurement.setData(parsed);
            measurement.setPageNumber(pageNumber);
            measurement.setDisplayedRecords(parsed.size());
        } catch (Exception ignored) {}

    }

    /**
     * Dispatch and process POST request based on provided parameter
     * @param entity json
     * @return JSON
     */
    @Post
    public String processPost(Representation entity) throws IOException {

        try {
            // first access data consumer
            DataConsumer consumer = new DataConsumer(defaultName, publisher);

            // process the message
            consumer.consume(entity.getText());

            return "Added for processing";

        } catch (Exception e) {
            return String.format("Error: %s", e.getMessage());
        }
    }
}