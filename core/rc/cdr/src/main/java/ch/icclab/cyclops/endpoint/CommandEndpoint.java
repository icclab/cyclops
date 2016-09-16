/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

package ch.icclab.cyclops.endpoint;

import ch.icclab.cyclops.consume.command.CommandConsumer;
import com.google.gson.Gson;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import java.io.IOException;

/**
 * Author: Skoviera
 * Created: 08/07/16
 * Description: Handle uploading data frames (the same way as with RabbitMQ)
 */
public class CommandEndpoint extends AbstractEndpoint {

    @Override
    public String getRoute() {
        return "/command";
    }

    /**
     * Dispatch and process POST request based on provided parameter
     * @param entity json
     * @return JSON
     */
    @Post
    public String processPost(Representation entity) throws IOException {

        try {
            // first access data consumer
            CommandConsumer consumer = new CommandConsumer();

            // process the message
            consumer.consume(entity.getText());

            // get execution status and response
            CommandConsumer.ExecutionStatus status = consumer.getStatus();
            Object response = consumer.getResponse();

            return (status.wasExecuted() && response != null)? new Gson().toJson(response) : status.getMessage();

        } catch (Exception e) {
            return String.format("Error: %s", e.getMessage());
        }
    }
}