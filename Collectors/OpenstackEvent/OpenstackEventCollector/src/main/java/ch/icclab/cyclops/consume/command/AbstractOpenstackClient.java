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

import ch.icclab.cyclops.dao.OpenStackEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackCredentials;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.DataLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.SelectQuery;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import java.util.List;

public abstract class AbstractOpenstackClient extends Command {
    final static Logger logger = LogManager.getLogger(RabbitMQStarter.class.getName());
    protected Status status = new Status();
    // singleton
    private OpenstackCredentials credentials;
    protected OSClient session;
    protected DbAccess db;

    protected abstract List<OpenStackEvent> getObjectsToSafe();


    public AbstractOpenstackClient() {
        credentials = Loader.getSettings().getOpenstackCredentials();
        session = obtainSession();
        db = new DbAccess();
    }

    /**
     * Ask for connection to OpenStack
     * @return session
     */
    private OSClient obtainSession() {
        DataLogger.log("Trying to obtain session");
        System.out.println("Trying to obtain session");
        try{
            String endpoint = credentials.getOpenstackEndpoint();
            String username = credentials.getOpenstackUsername();
            String password = credentials.getOpenstackPassword();
            String project = credentials.getOpenstackTenant();
            System.out.println(endpoint);
            String domain = "Default";
            if (endpoint.contains("v3")){
                DataLogger.log("Trying to set up connection with V3 endpoint");
                return OSFactory.builderV3()
                        .withConfig(Config.newConfig().withSSLVerificationDisabled())
                        .endpoint(endpoint)
                        .credentials(username, password, Identifier.byName(domain))
                        .scopeToProject(Identifier.byName(project), Identifier.byName(domain))
                        .authenticate();
            } else {
                DataLogger.log("Trying to set up connection with V2 endpoint");
                return OSFactory.builderV2()
                        .endpoint(endpoint)
                        .credentials(username, password)
                        .tenantName(project)
    //                    .perspective(Facing.ADMIN)
                        .authenticate();
            }
        } catch (Exception e){
            String message = "Openstack Session failed:" + e.getMessage();
            status.setServerError(message);
            logger.error(message);
            return null;
        }
    }

    @Override
    Status execute() {
        try {
            String message;
            List<OpenStackEvent> events = getObjectsToSafe();
            // we got list of Usage objects
            if (events != null) {
                for (OpenStackEvent event : events) {
                    // persist the usage data
                    DbAccess.PersistenceStatus persisted = persistEvent(event);
                    switch (persisted) {
                        case DB_DOWN:
                            message="Received event, but unable to persist (db is down)";
                            DataLogger.log(message);
                            status.setServerError(message);
                            break;

                        case INVALID_RECORDS:
                            message = "Received event, but some of them are invalid";
                            DataLogger.log(message);
                            status.setServerError(message);
                            break;

                        case OK:
                            message = "Received and persisted event";
                            DataLogger.log(message);
                            status.setSuccessful(message);
                            break;
                    }
                }
            }
        } catch (Exception ignored){}

        return status;

    }

    /**
     * Persist list to database
     * @param event of event data
     * @return status
     */
    private DbAccess.PersistenceStatus persistEvent(OpenStackEvent event) {

        // persist the list transactionally
        return db.storePersistentObject(event);
    }

    /**
     * Method to get all unique sources
     */
    protected List getSourceList(OpenStackEvent clazz){
        try{
            // select time_from OpenStackEvent table
            SelectQuery select = db.createSelectFrom(clazz.getTable());

            select.addDistinctOn(OpenStackEvent.SOURCE_FIELD);

            select.addConditions(OpenStackEvent.PROCESSED_FIELD.eq(false));

            return db.fetchUsingSelectStatement(select, clazz.getClass());

        } catch (Exception ignored) { return null; }
    }

}
