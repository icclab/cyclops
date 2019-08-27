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

import ch.icclab.cyclops.dao.Bill;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.jooq.Condition;
import org.jooq.SelectQuery;
import org.jooq.DeleteQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;

public class DeleteBills extends Command{
    private Long time_from;
    private Long time_to;

    /**
     * Delete Bill records
     *
     * @return status
     */
    @Override
    Status execute() {
        Status status = new Status();
        DeleteQuery delete = null;
        try {
            DbAccess db = new DbAccess();

            // select time_from Bill table
            delete = db.createDeleteFrom(Bill.TABLE);

            // time window selection
            delete.addConditions(Bill.TIME_FROM_FIELD.ge(inline(new Timestamp(time_from))));

            // include all CDRs, or only CDRs ending in the selected window
            delete.addConditions(Bill.TIME_TO_FIELD.le(inline(new Timestamp(time_to))));



            int deletion = db.executeDeleteStatement(delete);
            String message;
            if(deletion != -1){
                message = "Deleted affected records";
                status.setSuccessful(message);
            }

        } catch (Exception e) {
            CommandLogger.log(e.getMessage());
            status.setServerError(e.getMessage());
        } finally{
            assert delete != null;
            delete.close();
        }

        return status;


    }
}
