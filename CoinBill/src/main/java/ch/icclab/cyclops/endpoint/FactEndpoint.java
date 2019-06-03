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

import ch.icclab.cyclops.facts.FactMapping;
import ch.icclab.cyclops.facts.MappedFact;
import ch.icclab.cyclops.rule.RuleManagement;
import ch.icclab.cyclops.util.PrettyGson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 04/03/16
 * Description: Handle uploading facts to inference engine and retrieving answers
 */
public class FactEndpoint extends ServerResource {

    // logger
    final static Logger logger = LogManager.getLogger(FactEndpoint.class.getName());

    /**
     * This method is invoked in order to get command from API URL
     */
    public void doInit() {
        // log received message
        logger.trace("Dispatching RESTful API call for: /facts");
    }

    /**
     * Dispatch and process POST request based on provided parameter
     * @param entity json
     * @return JSON
     */
    @Post
    public String processPost(Representation entity){
        String response = "";

        try {
            // json string
            String content = entity.getText();

            // try to map just one fact
            List<MappedFact> listOfFacts= FactMapping.fromJson(content);

            if (listOfFacts == null) {
                response = "Zero facts loaded, as one of them is corrupted";
                logger.error(response);

                return response;
            }

            String message = String.format("%d fact(s) loaded", listOfFacts.size());

            // access rule management
            RuleManagement management = RuleManagement.getInstance();

            // load facts to working memory
            management.loadFacts(listOfFacts);

            // check whether we want to execute it or not
            String str = getQueryValue("execute");

            // by default we will execute it
            if (str == null || Boolean.parseBoolean(str)) {
                // execute rules on those facts
                List result = management.fireAllRulesNow();

                if (result != null) {
                    if (!result.isEmpty()) response = PrettyGson.toJson(result);
                    else response = message;
                }
            } else {
                message += " but rules not executed as requested";
                response = message;
            }

            logger.trace(message);
        } catch (IOException e) {
            response = String.format("Fact representation is invalid: %s", e.getMessage());
        } catch (Exception e) {
            // TODO what to do with the fact when developer's rule is failing?
            response = String.format("Rule execution based on inserted fact failed: %s", e.getMessage());
        }

        return response;
    }
}