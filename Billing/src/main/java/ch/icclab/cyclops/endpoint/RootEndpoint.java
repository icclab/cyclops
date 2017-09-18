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

import org.restlet.Response;
import org.restlet.resource.Get;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 21/01/16
 * Description: Serve application's version over root endpoint
 */
public class RootEndpoint extends AbstractEndpoint {

    private class Description {
        private String service;
        private String version;

        public Description(String service, String version) {
            this.service = service;
            this.version = version;
        }

        public String getService() {
            return service;
        }

        public String getVersion() {
            return version;
        }
    }

    @Get
    public Response root(){
        Response response = getResponse();
        String service = "Cyclops Billing";
        String version = "2.3.0";

        Description description = new Description(service, version);
        return new HTTPOutput(service, description).prepareResponse(response, false);
    }

    @Override
    public List<String> getRoutes() {
        List<String> list = new ArrayList<>();

        list.add("/");

        return list;
    }
}
