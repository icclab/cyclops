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

import ch.icclab.cyclops.dto.InstanceDTO;
import ch.icclab.cyclops.dto.ListInstanceDTO;
import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.orm.InstanceORM;
import ch.icclab.cyclops.resource.InstanceResource;
import ch.icclab.cyclops.rule.RuleException;
import ch.icclab.cyclops.rule.RuleManagement;
import ch.icclab.cyclops.util.PrettyGson;
import ch.icclab.cyclops.util.loggers.TimelineLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import java.io.IOException;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 08/02/16
 * Description: Handle adding, listing and removing of Instances based on stored Rule Templates
 */
public class RuleEndpoint extends ServerResource {

    // logger
    final static Logger logger = LogManager.getLogger(RuleEndpoint.class.getName());

    private Long id = null;

    /**
     * This method is invoked in order to get command from API URL
     */
    public void doInit() {
        try {
            id = Long.parseLong((String) getRequestAttributes().get("id"));
        } catch (Exception e) {
            id = null;
        }

        // log received message
        logger.trace("Dispatching RESTful API call for /rules");
    }

    /**
     * Dispatch and process GET request based on provided parameter
     * @return JSON
     */
    @Get
    public String processGetRequest() {

        // will be filled once dispatched
        String response = "";

        try {
            if (id == null) {
                // list all instances
                response = processList();
            } else {
                response = processView(id);
            }
        } catch (Exception e) {
            response = e.getMessage();
            logger.trace(response);
        }

        return response;
    }

    /**
     * Dispatch and process DELETE request
     * @return response
     */
    @Delete
    public String processDeleteRequest() {
        InstanceResource resource = new InstanceResource();

        String response = "";

        try {
            if (id == null) {
                response = "Instance ID not provided";
                logger.error(response);
            } else {
                // remove specified template
                Boolean status = resource.removeInstance(id);

                // prepare response
                if (status) {
                    response = String.format("Instance with ID %d deleted", id);
                    logger.trace(response);

                    // also log it into timeline
                    TimelineLogger.log(response);
                } else {
                    response = String.format("Instance with ID %d not found", id);
                    logger.error(response);
                }
            }
        } catch (DatabaseException | RuleException e) {
            response = e.getMessage();
            logger.trace(response);
        }

        return response;
    }

    /**
     * Dispatch and process POST request based on provided parameter
     * @param entity json
     * @return JSON
     */
    @Post
    public String processPostRequest(Representation entity) {
        // will be filled once dispatched
        String response = "";

        // process adding a new rule directly
        try {
            // for optional one time execution (without storing it into memory)
            String onetime = getQueryValue("onetime");
            if (onetime != null && Boolean.valueOf(onetime)) {
                response = executeRuleJustOnce(entity);
            } else {
                // now we know we actually want to persist it
                response = processAdd(entity);
            }
        } catch (IOException e) {
            response = String.format("Received rule representation is not valid: \"%s\"", e.getMessage());
            logger.trace(response);
        } catch (RuleException | DatabaseException e) {
            response = e.getMessage();
            logger.trace(response);
        } catch (Exception e) {
            response = String.format("Rule execution failed: %s", e.getMessage());
            logger.trace(response);
        }

        return response;
    }

    /**
     * Process LIST command and get list of templates
     * @return formatted list
     */
    private String processList() throws DatabaseException{
        // we are working with templates here
        InstanceResource resource = new InstanceResource();

        List<InstanceORM> list;
        try {
            Integer id = Integer.valueOf(getQueryValue("template"));
            list = resource.listInstancesForTemplate(id);
        } catch (NumberFormatException | NullPointerException e) {
            list = resource.listInstances();
        }

        // populate our own list of templates
        ListInstanceDTO formatted = new ListInstanceDTO(list);

        // format according to JSON structure
        return PrettyGson.toJson(formatted);
    }

    /**
     * Process ADD command and store provided template
     * @param entity as template
     * @return string message
     */
    private String processAdd(Representation entity) throws Exception {
        InstanceResource resource = new InstanceResource();

        InstanceORM instance = resource.addRule(entity.getText());
        String response = String.format("Rule \"%s\" added as id %d", instance.getName(), instance.getId());
        logger.trace(response);

        // also log it into timeline
        TimelineLogger.log(response);

        // in case that user asked to execute rules now
        String execute = getQueryValue("execute");

        // in case user wants rule instance to be executed immediately
        if (execute != null && Boolean.valueOf(execute)) {
            RuleManagement management = RuleManagement.getInstance();

            // execute rules based on present facts
            List list = management.fireAllRulesNow();

            // optionally return the response
            if (list != null) {
                if (!list.isEmpty()) response = PrettyGson.toJson(list);
                else response = String.format("Rule \"%s\" executed and added as id %d", instance.getName(), instance.getId());
            }
        }

        return response;
    }

    /**
     * Process VIEW command and display content of a template
     * @return template content
     */
    private String processView(Long id) throws RuleException, DatabaseException {
        logger.trace(String.format("Displaying content of an instance with id: %d", id));

        // get template
        InstanceResource resource = new InstanceResource();
        InstanceORM instance = resource.getInstance(id);

        if (instance != null) {
            // prepare template DTO object
            InstanceDTO instanceDTO = new InstanceDTO(instance.getId(), instance.getRule(), instance.getTemplateId(),
                    instance.getName(), instance.getAdded(), instance.getFieldsAsMap());

            // return it as a string
            return PrettyGson.toJson(instanceDTO);
        } else {
            String msg = String.format("Instance with id: %d was not found", id);
            logger.error(msg);

            throw new RuleException(msg);
        }
    }

    private String executeRuleJustOnce(Representation entity) throws IOException {
        String response = "";
        try {
            // create rule
            InstanceORM rule = new InstanceORM(entity.getText());

            // access instance of rule management
            RuleManagement management = RuleManagement.getInstance();

            // execute rule
            List list = management.fireJustOnce(rule);

            // log the status message
            response = String.format("Rule \"%s\" manually executed (just once)", rule.getName());
            logger.trace(response);
            TimelineLogger.log(response);

            // output result
            if (list != null && !list.isEmpty())
                response = PrettyGson.toJson(list);

        } catch (Exception e) {
            response = String.format("Couldn't execute (just once) received rule: %s", e.getMessage());
            logger.error(response);
        }

        return response;
    }
}