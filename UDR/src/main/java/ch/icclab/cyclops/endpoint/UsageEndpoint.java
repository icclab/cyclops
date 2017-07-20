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

import ch.icclab.cyclops.consume.data.UsageProcess;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 16.05.17
 * Description: POST usage into UDR microservice
 */
public class UsageEndpoint extends AbstractEndpoint {
    @Override
    public String getRoute() {
        return "/usage";
    }

    @Post
    public Response addUsage(JsonRepresentation representation) {
        // prepare response
        Response response = getResponse();
        HTTPOutput status = null;

        // extract JSON from the representation
        String json = getJSONText(representation);

        // if json is invalid, throw
        if (json == null || json.isEmpty())
            return new HTTPOutput(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE,"Payload too large").prepareResponse(response);

        // process received JSON usage in the same thread
        UsageProcess process = new UsageProcess(json);
        process.run();

        // was the operation successful?
        UsageProcess.Status usage = process.getStatus();
        if (usage.isParsed()) {
            if (usage.isPersisted())
                status = new HTTPOutput(Status.SUCCESS_CREATED, String.format("Stored %d records", usage.getNumberOfRecords()));
            else if (usage.isDbDown())
                status = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, String.format("Couldn't store %d records, db is down", usage.getNumberOfRecords()));
            else if (usage.isInvalid())
                status = new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, String.format("Couldn't store %d records, at least one of them is invalid (not unique, duplicated, has an empty or a missing field)", usage.getNumberOfRecords()));
            else status = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, "Unknown cause: usage valid/parsed but not persisted (while db is alive)");
        } else status = new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, "Couldn't parse, most likely invalid JSON");

        RESTLogger.log(String.format("%s %s", getRoute(), status.toString()));

        // return the status code and info
        return status.prepareResponse(response);
    }

    /**
     * Get JSON text from the Representation entity
     * @param entity json
     * @return String or null
     */
    private String getJSONText(JsonRepresentation entity) {
        try {
            return entity.getText();
        } catch (Exception e) {
            return null;
        }
    }
}