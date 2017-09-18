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

import ch.icclab.cyclops.dao.CDR;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.jooq.Condition;
import org.jooq.SelectQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09.05.17
 * Description: Flush CDRs command
 */
public class FlushCDRs extends Command {
    // mandatory fields
    private Long time_from;
    private Long time_to;
    private int run;
    private List<String> accounts;

    private class ClientException extends Exception {
        public ClientException(String message) {
            super(message);
        }
    }

    private class FlushingFinished {
        private String type;
        private long time_from;
        private long time_to;
        private int run;
        private List<String> accounts;

        public FlushingFinished(long time_from, long time_to, int run, List<String> accounts) {
            this.type = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
            this.run = run;
            this.accounts = accounts;
        }

        public String getType() {
            return type;
        }

        public long getTime_from() {
            return time_from;
        }

        public long getTime_to() {
            return time_to;
        }

        public int getRun() {
            return run;
        }

        public List<String> getAccounts() {
            return accounts;
        }
    }

    /**
     * Flush CDR records
     * @return number of inserted CDRs
     */
    @Override
    Status execute(){
        Status status = new Status();

        try {
            // sanity checks first
            Condition accountCondition = sanityCheckAndWhereAccounts();

            DbAccess db = new DbAccess();

            // select time_from CDR table
            SelectQuery select = db.createSelectFrom(CDR.TABLE, inline(run).as("run"), CDR.METRIC_FIELD, CDR.ACCOUNT_FIELD, CDR.TIME_FROM_FIELD, CDR.TIME_TO_FIELD,
                    CDR.CHARGE_FIELD, CDR.CURRENCY_FIELD, CDR.DATA_FIELD);

            // time window selection
            select.addConditions(CDR.TIME_FROM_FIELD.ge(inline(new Timestamp(time_from))));

            // include all CDRs, or only CDRs ending in the selected window
            select.addConditions(CDR.TIME_TO_FIELD.le(inline(new Timestamp(time_to))));

            // filter list of accounts
            select.addConditions(accountCondition);

            // fetch and map into list of CDRs
            List listToPush = db.fetchUsingSelectStatement(select, CDR.class);

            String message;

            if (listToPush== null) throw new Exception("Error occurred, check your database connection");
            else {
                FlushingFinished finished = new FlushingFinished(time_from, time_to, run, accounts);
                int numberOfRecords = listToPush.size();
                boolean pushed;

                // there are no charge records
                if (listToPush.isEmpty()) {
                    pushed = Messenger.broadcast(finished);
                } else {
                    // transform CDR's data field time_from PGObject time_to Map
                    listToPush = CDR.applyPGObjectDataFieldToMapTransformation(listToPush);

                    // add object marking the list finished
                    listToPush.add(finished);

                    // broadcast to the next microservice
                    pushed = Messenger.broadcast(listToPush);
                }

                if (pushed) {
                    message = String.format("%d CDRs flushed (starting and ending between %s and %s period)", numberOfRecords, new Timestamp(time_from), new Timestamp(time_to));
                    status.setSuccessful(message);
                } else throw new Exception(String.format("Could not flush %d CDRs, RabbitMQ is down", numberOfRecords));
            }

            CommandLogger.log(message);

        } catch (ClientException e) {
            CommandLogger.log(e.getMessage());
            status.setClientError(e.getMessage());
        } catch (Exception e) {
            CommandLogger.log(e.getMessage());
            status.setServerError(e.getMessage());
        }

        return status;
    }

    /**
     * Check parameters and get account condition
     * @return account Condition
     * @throws ClientException as ClientError
     */
    private Condition sanityCheckAndWhereAccounts() throws ClientException {
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) throw new ClientException("Invalid FROM and TO (unit to be in milliseconds)");
        else if (accounts == null || accounts.isEmpty()) throw new ClientException("Empty or missing list of accounts");
        else {
            List<Condition> conditions = new ArrayList<>();
            for (String account: accounts) {
                if (account == null || account.isEmpty()) {
                    throw new ClientException("Invalid account (must be list of Strings)");
                }

                // add this account to the condition
                conditions.add(CDR.ACCOUNT_FIELD.eq(inline(account)));
            }

            Condition where = null;

            // create an account condition that goes into WHERE clause
            boolean first = true;
            for (Condition condition : conditions) {
                if (first) {
                    where = condition;
                    first = false;
                } else where = where.or(condition);
            }

            return where;
        }
    }
}
