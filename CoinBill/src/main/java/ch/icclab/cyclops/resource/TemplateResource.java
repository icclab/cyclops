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
import ch.icclab.cyclops.persistence.orm.TemplateORM;
import ch.icclab.cyclops.util.RegexParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 10/02/16
 * Description: Work with Templates for Rule Engine
 */
public class TemplateResource {

    // logger
    final static Logger logger = LogManager.getLogger(TemplateResource.class.getName());

    // persist objects with hibernate
    private HibernateClient hibernate = HibernateClient.getInstance();

    /**
     * Add a new template
     * @param content of template
     * @return template
     */
    public TemplateORM addTemplate(String content) throws DatabaseException{
        // prepare template
        String name = RegexParser.getNameFromTemplate(content);
        logger.trace(String.format("Adding a template with name \"%s\"", name));

        TemplateORM template = new TemplateORM(content, name);

        // save template
        hibernate.persistObject(template);

        return template;
    }

    /**
     * List available templates
     * @return list
     */
    public List<TemplateORM> listTemplates() throws DatabaseException{
        logger.trace("Listing templates");

        // construct query
        String query = QueryHelper.createListQuery(TemplateORM.class);

        // execute query
        return hibernate.executeQuery(query);
    }

    /**
     * Delete template
     * @param id of template
     * @return invalid template, nothing to delete
     */
    public boolean removeTemplate(Long id) throws DatabaseException {
        logger.trace("Removing template with id: " + id.toString());

        // execute query and get number of rows affected
        TemplateORM template = new TemplateORM(id);

        return hibernate.deleteObject(template);
    }

    /**
     * Get template
     * @param id of template
     * @return template or null
     */
    public TemplateORM getTemplate(Long id) throws DatabaseException{
        logger.trace("Getting template content with id: " + id.toString());

        // get object
        Object obj = hibernate.getObject(TemplateORM.class, id);

        // return template or null
        return (obj != null)? (TemplateORM) obj : null;
    }
}
