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

import ch.icclab.cyclops.dao.PersistentObject;
import org.jooq.*;
import org.jooq.util.postgres.PostgresDSL;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09.05.17
 * Description: Database access and its operations
 */
public class DbAccess {

    private static SQLDialect SQL_DIALECT = SQLDialect.POSTGRES_9_5;

    public enum PersistenceStatus {
        DB_DOWN, INVALID_RECORDS, OK
    }

    /**
     * Pings the underlying database
     * @return ping status
     */
    public boolean ping() {
        try (Connection connection = DbPool.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create Select statement
     * @param table to select from
     * @param fields optional fields and aggregates
     * @return select step
     */
    public SelectQuery createSelectFrom(Table table, Field ... fields) {
        // get Postgres context
        DSLContext context = PostgresDSL.using(SQL_DIALECT);

        // create select query and optionally add specified fields
        SelectQuery<?> select = context.selectQuery();
        if (fields.length > 0) select.addSelect(fields);

        // from table
        select.addFrom(table);

        return select;
    }

    /**
     * Create Insert INTO query
     * @param table to insert data to
     * @return insert query
     */
    public InsertQuery createInsertInto(Table<?> table) {
        return PostgresDSL.using(SQL_DIALECT).insertQuery(table);
    }

    /**
     * Execute insert statement
     * @param query to be executed
     * @return number of inserted records
     */
    public int executeInsertStatement(InsertQuery query) {
        try (Connection connection = DbPool.getConnection()) {
            return PostgresDSL.using(connection, SQL_DIALECT).execute(query);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Execute select statement
     * @param select statement
     * @return result or null
     */
    public <T> List<T> fetchUsingSelectStatement(SelectQuery<?> select, Class<T> clazz) {
        try (Connection connection = DbPool.getConnection()) {
            DSLContext context = PostgresDSL.using(connection, SQL_DIALECT);
            return context.fetch(select).into(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Store objects implementing PersistentObject interface
     * @param objects list
     * @return status
     */
    public PersistenceStatus storePersistentObjects(List<? extends PersistentObject> objects) {
        // edge case sanity check
        if (objects == null || objects.isEmpty()) return PersistenceStatus.INVALID_RECORDS;

        // note status of the operation
        PersistenceStatus status = PersistenceStatus.OK;

        try (Connection connection = DbPool.getConnection()) {
            // use the connection and create DSL context
            DSLContext context = PostgresDSL.using(connection, SQL_DIALECT);

            // transactional operation
            context.transaction(configuration -> {
                // reuse open context
                DSLContext ctx = PostgresDSL.using(configuration);

                // insert the object directly
                if (objects.size() == 1) getInsertStatement(ctx, objects.get(0)).execute();

                    // insert objects in batch
                else {
                    // insert statement holder
                    boolean alreadyInitialized = false;
                    BatchBindStep bindStep = null;

                    // iterate over records
                    for (PersistentObject object : objects) {

                        // initialize the insert statement holder
                        if (!alreadyInitialized) {
                            bindStep = ctx.batch(getInsertStatement(ctx, object));
                            alreadyInitialized = true;
                        }

                        // add values to the insert step
                        bindStep = bindStep.bind(object.getValues());
                    }

                    // finally persist them
                    bindStep.execute();
                }
            });
        } catch (NullPointerException e) {
            // getting connection returned null
            status = PersistenceStatus.DB_DOWN;
        } catch (Exception e) {
            // statement execution on valid connection failed
            status = PersistenceStatus.INVALID_RECORDS;
        }

        return status;
    }

    /**
     * Store object implementing PersistentObject interface
     * @param object list
     * @return status
     */
    public PersistenceStatus storePersistentObject(PersistentObject object) {
        return storePersistentObjects(Collections.singletonList(object));
    }

    /**
     * Create insert statement with specified fields and values
     * @param context for the statement
     * @param object implementing Persistent Object interface
     * @return statement
     */
    private InsertFinalStep getInsertStatement(DSLContext context, PersistentObject object) {
        return context.insertInto(object.getTable(), object.getFields()).values(object.getValues());
    }

    /**
     * Shut down the database access
     */
    public void shutDown() {
        DbPool.shutDown();
    }
}
