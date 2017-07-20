package ch.icclab.cyclops.resource;
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

import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.QueryHelper;
import ch.icclab.cyclops.persistence.orm.InstanceORM;
import ch.icclab.cyclops.persistence.orm.TemplateORM;
import ch.icclab.cyclops.rule.RuleException;
import ch.icclab.cyclops.rule.RuleManagement;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 10/02/16
 * Description: Work with Templates for Rule Engine
 */
public class InstanceResource {

    // logger
    final static Logger logger = LogManager.getLogger(InstanceResource.class.getName());

    // persist objects with hibernate
    private HibernateClient hibernate = HibernateClient.getInstance();

    /**
     * Instantiate a template
     * @param id of template
     * @param content of fields
     * @return id or exception
     */
    public InstanceORM instantiateTemplate(Long id, String content) throws RuleException, DatabaseException {
        logger.trace(String.format("Instantiating a template with ID %d", id));

        // get template and its fields
        TemplateResource templateResource = new TemplateResource();
        TemplateORM template = templateResource.getTemplate(id);;

        if (template == null) {
            throw new RuleException(String.format("Template with id %d is not present", id));
        }

        // map fields to object
        Map map = (Map) new Gson().fromJson(content, Object.class);

        // validate fields
        if (validateFields(map, template.getFieldsAsList())) {

            // create an instance
            InstanceORM instance = new InstanceORM(template, map);

            // add to production memory of the inference engine
            RuleManagement management = RuleManagement.getInstance();

            management.addRule(instance);

            // store it to memory and update ID
            hibernate.persistObject(instance);

            return instance;
        } else {
            throw new RuleException(String.format("Couldn't instantiate template %d with provided fields", id));
        }
    }

    /**
     * Add a rule
     * @param ruleContent content
     * @return id or null
     */
    public InstanceORM addRule(String ruleContent) throws RuleException, DatabaseException {
        InstanceORM instance = new InstanceORM(ruleContent);
        logger.trace(String.format("Adding a new rule with name \"%s\"", instance.getName()));

        // add to production memory of the inference engine
        RuleManagement management = RuleManagement.getInstance();

        management.addRule(instance);

        // persist it and return ID
        hibernate.persistObject(instance);

        return instance;
    }

    /**
     * Validate that template fields are present
     * @param map content to be used
     * @param fields to be validated
     * @return true or false
     */
    private Boolean validateFields(Map map, List<String> fields) {
        if (map != null) {
            // validate that we have everything needed
            for (String field: fields) {

                // check whether required field is present in received content
                if (!map.containsKey(field)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * List available templates for specified template
     * @param id template
     * @return list
     */
    public List<InstanceORM> listInstancesForTemplate(Integer id) throws DatabaseException{
        logger.trace(String.format("Listing instances for template %d", id));

        // construct query
        String query = QueryHelper.createListQuery(InstanceORM.class, id);

        // execute query
        return hibernate.executeQuery(query);
    }

    /**
     * List all available instances
     * @return list
     */
    public List<InstanceORM> listInstances() throws DatabaseException {
        logger.trace("Listing all instances");

        // construct query
        String query = QueryHelper.createListQuery(InstanceORM.class);

        // execute query
        return hibernate.executeQuery(query);
    }

    /**
     * Delete template
     * @param id of instance
     * @return whether instance was found or not
     */
    public boolean removeInstance(Long id) throws RuleException, DatabaseException {
        logger.trace("Removing instance with id: " + id.toString());

        // execute query and get number of rows affected
        InstanceORM instance = getInstance(id);
        if (instance != null) {
            RuleManagement management = RuleManagement.getInstance();

            // remove from both working memory and persistent storage
            if (management.removeRule(instance)) return hibernate.deleteObject(instance);
            else throw new RuleException("Couldn't delete rule from the working memory, leaving it in the database");
        } else return false;
    }

    /**
     * Get template
     * @param id of instance
     * @return template or null
     */
    public InstanceORM getInstance(Long id) throws DatabaseException {
        logger.trace("Getting instance content with id: " + id.toString());

        // get object
        Object obj = hibernate.getObject(InstanceORM.class, id);

        // return template or null
        return (obj != null)? (InstanceORM) obj : null;
    }
}
