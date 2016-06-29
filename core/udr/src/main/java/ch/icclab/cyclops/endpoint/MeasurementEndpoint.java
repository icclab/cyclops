package ch.icclab.cyclops.endpoint;
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

import ch.icclab.cyclops.consume.data.DataConsumer;
import ch.icclab.cyclops.dto.Measurement;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.PrettyGson;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 17/05/16
 * Description: Endpoint for measurements
 */
public class MeasurementEndpoint extends ServerResource{
    final static Logger logger = LogManager.getLogger(MeasurementEndpoint.class.getName());

    public static String ENDPOINT = "/measurement";
    public static String ATTRIBUTE = "name";

    // supported functions
    private final static String FUN_COUNT = "count";

    // supported params
    private final static String PARAM_PAGE = "page";
    private final static String PARAM_FROM = "from";
    private final static String PARAM_TO = "to";

    private final APICallCounter counter = APICallCounter.getInstance();
    private final InfluxDBClient dbClient = InfluxDBClient.getInstance();
    private String attribute;

    /**
     * Copy parameters for this particular request
     */
    public void doInit() {
        attribute = (String) getRequestAttributes().get(ATTRIBUTE);
    }

    @Get
    public String processGet(){
        counter.increment(ENDPOINT);

        // access query user specified
        Map params = getQuery().getValuesMap();

        // prepare measurement
        Measurement measurement = new Measurement(attribute, params);

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
        return PrettyGson.toJson(measurement);
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
                builder.timeFrom(from, DataConsumer.TIME_UNIT);
            }

            params.remove(PARAM_FROM);
        }

        // date range specification TO
        if (params.containsKey(PARAM_TO)) {
            Long to = NumberUtils.toLong(params.get(PARAM_TO), 0);

            // only if it makes sense
            if (to > 0) {
                builder.timeTo(to, DataConsumer.TIME_UNIT);
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
            List<Map> countResult = dbClient.executeQuery(builder.count(InfluxDBCredentials.COUNTER_FIELD_NAME));

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

        // reset selected fields first
        builder.resetSelectClause();

        // add limit and offset, plus execute query
        List<Map> parsed = dbClient.executeQuery(builder.limit(pageLimit).offset(pageNumber*pageLimit));

        // update measurement
        measurement.setData(parsed);
        measurement.setPageNumber(pageNumber);
        measurement.setDisplayedRecords(parsed.size());
    }
}
