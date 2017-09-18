package ch.icclab.cyclops.consume.command;
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
import ch.icclab.cyclops.dao.Usage;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.SelectQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.sum;


/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09.05.17
 * Description: Generate UDRs based on list of metrics, in case of multiple usage records, select the highest
 */
public class GenerateMaxUDRs extends Command {
    // mandatory fields
    private Long time_from;
    private Long time_to;
    private List<String> metrics;

    /**
     * Look at usage records and generate list of UDRs
     * @return number of inserted UDRs
     */
    @Override
    Status execute() {
        Status status = new Status();

        // sanity checks first
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) {
            status.setClientError("Invalid FROM and TO (unit to be in milliseconds)");
            return status;
        }

        if (metrics == null || metrics.isEmpty()) {
            status.setClientError("Provide list of metrics to generate UDRs");
            return status;
        }

        DbAccess db = new DbAccess();

        // create INSERT INTO statement
        InsertQuery<?> insert = db.createInsertInto(UDR.TABLE);

        // prepare fields
        Field[] fields = {UDR.METRIC_FIELD, UDR.ACCOUNT_FIELD, UDR.USAGE_FIELD,
                UDR.TIME_FROM_FIELD, UDR.TIME_TO_FIELD, UDR.UNIT_FIELD, UDR.DATA_FIELD};

        // prepare select command
        SelectQuery select = db.createSelectFrom(Usage.TABLE, Usage.METRIC_FIELD, Usage.ACCOUNT_FIELD, Usage.USAGE_FIELD,
                inline(new Timestamp(time_from)), inline(new Timestamp(time_to)), Usage.UNIT_FIELD, Usage.DATA_FIELD);

        // add where predicate for TIME and WHERE METRICS
        select.addConditions(Usage.TIME_FIELD.between(inline(new Timestamp(time_from)), inline(new Timestamp(time_to))));
        select.addConditions(onlySelectTheseMetrics(metrics));

        // aggregate based on group by
        select.addGroupBy(Usage.METRIC_FIELD, Usage.ACCOUNT_FIELD, Usage.USAGE_FIELD, Usage.UNIT_FIELD, Usage.DATA_FIELD);

        // modify insert command with list of fields and select statement
        insert.setSelect(fields, select);

        // execute the insert
        int ret = db.executeInsertStatement(insert);

        String message;

        // and handle the status
        if (ret < 0) {
            message = "Problem with the database access, could not generate UDRs";
            status.setServerError(message);
        } else {
            message = String.format("%d UDRs generated for period %s and %s", ret, new Timestamp(time_from), new Timestamp(time_to));
            status.setSuccessful(message);
        }

        CommandLogger.log(message);

        return status;
    }

    /**
     * Create WHERE condition with list of metrics
     * @param metrics to query for
     * @return condition
     */
    private Condition onlySelectTheseMetrics(List<String> metrics) {
        List<Condition> conditions = new ArrayList<>();

        // add all metrics into condition list
        metrics.forEach(metric -> conditions.add(Usage.METRIC_FIELD.eq(inline(metric))));

        Condition where = null;
        boolean first = true;

        // now glue them via OR
        for (Condition condition: conditions) {
            if (first) {
                where = condition;
                first = false;
            } else where = where.or(condition);
        }

        return where;
    }
}
