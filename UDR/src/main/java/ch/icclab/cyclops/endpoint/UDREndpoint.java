package ch.icclab.cyclops.endpoint;
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

import ch.icclab.cyclops.dao.UDR;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import org.jooq.SelectQuery;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.Get;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.inline;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 16.05.17
 * Description: GET UDRs
 */
public class UDREndpoint extends AbstractEndpoint {
    // mandatory fields
    private static String TIME_FROM = UDR.TIME_FROM_FIELD.getName();
    private static String TIME_TO = UDR.TIME_TO_FIELD.getName();

    // optional account
    private static String ACCOUNT = UDR.ACCOUNT_FIELD.getName();

    // optional metric
    private static String METRIC = UDR.METRIC_FIELD.getName();

    // optional unit
    private static String UNIT = UDR.UNIT_FIELD.getName();

    // optional page number
    private static String PAGE = "page";
    private int pageLimit;

    private class Envelope {
        private List<UDR> data;
        private int pageLimit;
        private int selectedPage;
        private long recordsShown;

        public Envelope(List<UDR> UDRs, int pageLimit, int selectedPage) {
            this.data = UDRs;
            this.pageLimit = pageLimit;
            this.selectedPage = selectedPage;
            this.recordsShown = (UDRs != null)? UDRs.size(): 0;
        }

        public List<UDR> getData() {
            return data;
        }
        public int getPageLimit() {
            return pageLimit;
        }
        public int getSelectedPage() {
            return selectedPage;
        }
        public long getRecordsShown() {
            return recordsShown;
        }
    }

    public UDREndpoint() {
        pageLimit = Loader.getSettings().getDatabaseCredentials().getDatabasePageLimit();
    }

    @Override
    public String getRoute() {
        return "/udr";
    }

    @Get("json")
    public Response getUDRs() {
        // prepare response
        Response response = getResponse();
        HTTPOutput output = null;

        // extract parameters such as time from, to, account name
        Map params = getQuery().getValuesMap();

        // all optional parameters
        Long time_from = extractLong(params, TIME_FROM, -1L);
        if (time_from == null)
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, String.format("Invalid %s timestamp condition (provide milliseconds)",
                    UDR.TIME_FROM_FIELD.getName())).prepareResponse(response);

        Long time_to = extractLong(params, TIME_TO, -1L);
        if (time_to == null)
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, String.format("Invalid %s timestamp condition (provide milliseconds)",
                UDR.TIME_TO_FIELD.getName())).prepareResponse(response);

        // if time_to was specified but time_from is still higher number
        if (time_to >= 0 && time_from > time_to)
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, String.format("%s timestamp cannot be higher than %s", UDR.TIME_FROM_FIELD.getName(),
                    UDR.TIME_TO_FIELD.getName())).prepareResponse(response);

        Integer selectedPage = extractInt(params, PAGE, 0);
        if (selectedPage == null || selectedPage < 0)
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid page number").prepareResponse(response);

        String account = extractString(params, ACCOUNT, null);
        if (account != null && account.isEmpty())
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid account name").prepareResponse(response);

        String metric = extractString(params, METRIC, null);
        if (metric != null && metric.isEmpty())
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid metric name").prepareResponse(response);

        String unit = extractString(params, UNIT, null);
        if (unit != null && unit.isEmpty())
            return new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid unit").prepareResponse(response);

        // create a select query
        DbAccess db = new DbAccess();
        SelectQuery select = db.createSelectFrom(UDR.TABLE);

        // from time window
        if (time_from > -1) select.addConditions(UDR.TIME_FROM_FIELD.ge(inline(new Timestamp(time_from))));

        // to time window
        if (time_to > -1 && time_from <= time_to) select.addConditions(UDR.TIME_TO_FIELD.lt(inline(new Timestamp(time_to))));

        // filter by account
        if (account != null && !account.isEmpty()) select.addConditions(UDR.ACCOUNT_FIELD.eq(inline(account)));

        // filter by metric
        if (metric != null && !metric.isEmpty()) select.addConditions(UDR.METRIC_FIELD.eq(inline(metric)));

        // filter by unit
        if (unit != null && !unit.isEmpty()) select.addConditions(UDR.UNIT_FIELD.eq(inline(unit)));

        // offset and limit for pagination
        select.addLimit(selectedPage * pageLimit, pageLimit);

        // fetch from database
        List<UDR> UDRs= db.fetchUsingSelectStatement(select, UDR.class);

        if (UDRs == null) {
            output = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, "Could not fetch UDRs, most likely database is down");
            response = output.prepareResponse(response);
        } else {
            // transform UDR's data field time_from PGObject time_to Map
            UDRs = UDR.applyPGObjectDataFieldToMapTransformation(UDRs);

            output = new HTTPOutput(String.format("Fetched %d UDRs", UDRs.size()), new Envelope(UDRs, pageLimit, selectedPage));
            response = output.prepareResponse(response, false);
        }

        RESTLogger.log(String.format("%s %s", getRoute(), output.toString()));

        return response;
    }

    /**
     * Extract Long from provided map
     * @param map containing key
     * @param key to search for
     * @param defaultIfMissing to supplement
     * @return parsed value, null if invalid, default value if missing
     */
    private Long extractLong(Map map, String key, long defaultIfMissing) {
        try {
            return (map != null && map.containsKey(key))? Long.parseLong((String) map.get(key)): defaultIfMissing;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Extract int from provided map
     * @param map containing key
     * @param key to search for
     * @param defaultIfMissing to supplement
     * @return parsed or default value
     */
    private Integer extractInt(Map map, String key, int defaultIfMissing) {
        try {
            return (map != null && map.containsKey(key))? Integer.parseInt((String) map.get(key)): defaultIfMissing;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract String from provided map
     * @param map containing key
     * @param key to search for
     * @param defaultIfMissing to supplement
     * @return parsed or default value
     */
    private String extractString(Map map, String key, String defaultIfMissing) {
        return (map != null && map.containsKey(key))? (String) map.get(key): defaultIfMissing;
    }
}
