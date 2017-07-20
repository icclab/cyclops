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

import ch.icclab.cyclops.dto.ListTemplateDTO;
import ch.icclab.cyclops.dto.TemplateDTO;
import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.orm.InstanceORM;
import ch.icclab.cyclops.persistence.orm.TemplateORM;
import ch.icclab.cyclops.resource.InstanceResource;
import ch.icclab.cyclops.resource.TemplateResource;
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
 * Description: Handle adding, listing and removing Drools templates
 */
public class TemplateEndpoint extends ServerResource {

    // logger
    final static Logger logger = LogManager.getLogger(TemplateEndpoint.class.getName());

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
        logger.trace("Dispatching RESTful API call for templates");
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
                // list templates
                response = processList();
            } else {
                // show selected template
                response = processView(id);
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

        try {
            // dispatch based on received command
            if (id == null) {
                // add new template
                response = processAdd(entity);
            } else {
                // instantiate a template
                response = processInstantiation(id, entity);
            }
        } catch (IOException e) {
            response = String.format("Received template representation is not valid: \"%s\"", e.getMessage());
        } catch (DatabaseException | RuleException e) {
            response = e.getMessage();
        } catch (Exception e) {
            response = String.format("Rule execution based on template instantiation failed %s", e.getMessage());
        }

        return response;
    }

    /**
     * Dispatch and process DELETE request
     * @return response
     */
    @Delete
    public String processDeleteRequest() {
        TemplateResource resource = new TemplateResource();

        String response = "";

        try {
            if (id == null) {
                response = "Template ID not provided";
                logger.error(response);
            } else {
                // remove specified template
                boolean status = resource.removeTemplate(id);

                // prepare response
                if (status) {
                    response = String.format("Template with ID %d deleted", id);
                    logger.trace(response);

                    // also log it into timeline
                    TimelineLogger.log(response);

                    return response;
                } else {
                    response = String.format("Template with ID %d not found", id);
                    logger.error(response);
                }
            }
        } catch (DatabaseException e) {
            response = e.getMessage();
            logger.error(response);
        }

        return response;
    }

    /**
     * Instantiate a template based on provided values and template ID
     * @param id of template
     * @param content as fields
     * @return response
     */
    private String processInstantiation(Long id, Representation content) throws Exception {
        InstanceResource resource = new InstanceResource();

        // instantiate template
        InstanceORM instance = resource.instantiateTemplate(id, content.getText());

        String response = String.format("Template %d instantiated as Rule %d with parameters %s", id, instance.getId(), instance.getFieldsAsMap().toString());
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

            // return either list of updated facts or success message
            if (list != null) {
                if (!list.isEmpty()) response = PrettyGson.toJson(list);
                else response = String.format("Rule \"%s\" executed and instantiated with id %d from template %d with parameters %s", instance.getName(), instance.getId(), id, instance.getFieldsAsMap().toString());
            }
        }

        return response;
    }

    /**
     * Process LIST command and get list of templates
     * @return formatted list
     */
    private String processList() throws DatabaseException{
        // we are working with templates here
        TemplateResource resource = new TemplateResource();

        // list all templates
        List<TemplateORM> list = resource.listTemplates();

        // populate our own list of templates
        ListTemplateDTO formatted = new ListTemplateDTO(list);

        // format according to JSON structure
        return PrettyGson.toJson(formatted);
    }

    /**
     * Process ADD command and store provided template
     * @param entity as template
     * @return string message
     */
    private String processAdd(Representation entity) throws IOException, RuleException, DatabaseException {
        TemplateResource resource = new TemplateResource();

        // let's add it as a new template
        TemplateORM template = resource.addTemplate(entity.getText());
        if (template.getId() >= 0) {
            String response = String.format("Template \"%s\" added with ID: %d", template.getName(), template.getId());
            logger.trace(response);

            // also log it into timeline
            TimelineLogger.log(response);

            return response;
        } else {
            String msg = "Couldn't add specified template";
            logger.error(msg);
            throw new RuleException(msg);
        }
    }

    /**
     * Process VIEW command and display content of a template
     * @param id of template
     * @return response
     */
    private String processView(Long id) throws RuleException, DatabaseException {
        logger.trace(String.format("Displaying content of a template with id: %d", id));

        TemplateResource resource = new TemplateResource();

        // get template
        TemplateORM template = resource.getTemplate(id);

        if (template != null) {

            // prepare template DTO object
            TemplateDTO templateDTO = new TemplateDTO();
            templateDTO.setId(template.getId());
            templateDTO.setTemplate(template.getRuleTemplate());
            templateDTO.setName(template.getName());
            templateDTO.setFields(template.getFieldsAsList());
            templateDTO.setAdded(template.getAdded());

            // return it as a string
            return PrettyGson.toJson(templateDTO);
        } else {
            String msg = String.format("Template with id: %d was not found", id);
            logger.error(msg);
            throw new RuleException(msg);
        }
    }
}