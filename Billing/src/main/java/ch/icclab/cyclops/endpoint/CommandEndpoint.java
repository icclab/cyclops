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

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.consume.command.CommandConsumer;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 08/07/16
 * Description: Handle uploading data frames (the same way as with RabbitMQ)
 */
public class CommandEndpoint extends AbstractEndpoint {

    @Override
    public List<String> getRoutes() {
        List<String> list = new ArrayList<>();

        list.add("/command");

        return list;
    }

    /**
     * Dispatch and process POST request based on provided parameter
     */
    @Post
    public Response processPost(JsonRepresentation representation) {
        // prepare response
        Response response = getResponse();
        HTTPOutput output = null;

        // extract JSON from the representation
        String json = getJSONText(representation);

        // if json is invalid, throw
        if (json == null || json.isEmpty())
            return new HTTPOutput(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE,"Payload too large").prepareResponse(response);

        // first access data consumer
        CommandConsumer consumer = new CommandConsumer();

        // process the message
        consumer.consume(json, null, null);

        // get execution status and response
        Command.Status command = consumer.getStatus();

        // status code and info
        if (command.hasSucceeded()) output = new HTTPOutput(command.getDescription(), command.getOutput());
        else if (command.hadClientError()) output = new HTTPOutput(Status.CLIENT_ERROR_BAD_REQUEST, command.getDescription());
        else if (command.hadServerError()) output = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, command.getDescription());
        else output = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, "Unknown state in command execution");

        RESTLogger.log(String.format("%s %s", getRoutes(), output.toString()));

        return output.prepareResponse(response);
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