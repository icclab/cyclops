/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.application;

import ch.icclab.cyclops.endpoint.*;
import ch.icclab.cyclops.util.RegexParser;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 21/01/16
 * Description: Application class that acts as router to service endpoints
 */
public class Service {

    final static Logger logger = LogManager.getLogger(Service.class.getName());

    // Router for registering api endpoints
    private Router router;

    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     */
    public Restlet createInboundRoot() throws Exception {

        logger.trace("Initialising UDR microservice and creating routes");

        router = attachRoutes();

        logger.trace("Routes for UDR microservice successfully created");

        return router;
    }

    /**
     * Attach routes that extend AbstractEndpoint class
     * @return router
     */
    private Router attachRoutes() {
        Router router = new Router();

        List<Class> list = new ArrayList<>();

        try {
            // find all endpoints and add them to the list
            list.addAll(new ClassesInPackageScanner().setResourceNameFilter((packageName, fileName) -> !AbstractEndpoint.class.getSimpleName().
                    equals(RegexParser.getFileName(fileName))).scan(AbstractEndpoint.class.getPackage().getName()));
        } catch (Exception ignored) {}

        // create routes
        for (Class clazz : list) {
            // only those which extend Endpoint
            if (AbstractEndpoint.class.isAssignableFrom(clazz)) {
                try {
                    // get endpoint's route path
                    String route = ((AbstractEndpoint) clazz.newInstance()).getRoute();
                    router.attach(route, clazz);
                } catch (Exception ignored) {}
            }
        }

        return router;
    }
}
