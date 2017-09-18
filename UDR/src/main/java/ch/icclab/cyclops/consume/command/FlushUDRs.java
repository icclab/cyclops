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
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.jooq.SelectQuery;

import java.sql.Timestamp;
import java.util.List;

import static org.jooq.impl.DSL.inline;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09.05.17
 * Description: Flush UDRs command
 */
public class FlushUDRs extends Command {
    // mandatory fields
    private Long time_from;
    private Long time_to;

    /**
     * Flush UDR records
     * @return number of inserted UDRs
     */
    @Override
    Status execute(){
        Status status = new Status();

        // sanity checks first
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) {
            status.setClientError("Invalid FROM and TO (unit to be in milliseconds)");
            return status;
        }

        DbAccess db = new DbAccess();

        // select time_from UDR table
        SelectQuery select = db.createSelectFrom(UDR.TABLE);

        // time window selection
        select.addConditions(UDR.TIME_FROM_FIELD.ge(inline(new Timestamp(time_from))));

        // include all UDRs, or only UDRs ending in the selected window
        select.addConditions(UDR.TIME_TO_FIELD.le(inline(new Timestamp(time_to))));

        // fetch and map into list of UDRs
        List<UDR> UDRs= db.fetchUsingSelectStatement(select, UDR.class);

        String message;

        if (UDRs == null) {
            message = "Error occurred, check your database connection";
            status.setServerError(message);
        } else if (UDRs.isEmpty()) {
            message = String.format("Zero UDRs to flush (starting and ending between %s and %s period)", new Timestamp(time_from), new Timestamp(time_to));
            status.setSuccessful(message);
        } else {
            // transform UDR's data field time_from PGObject time_to Map
            UDRs = UDR.applyPGObjectDataFieldToMapTransformation(UDRs);
            if (Messenger.broadcast(UDRs)) {
                message = String.format("Flushed %d UDRs (starting and ending between %s and %s period)", UDRs.size(), new Timestamp(time_from), new Timestamp(time_to));
                status.setSuccessful(message);
            } else {
                message = String.format("Could not flush %d UDRs, RabbitMQ is down", UDRs.size());
                status.setServerError(message);
            }
        }

        CommandLogger.log(message);

        return status;
    }
}
