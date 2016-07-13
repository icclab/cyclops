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
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.PrettyGson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 08/07/16
 * Description: Handle uploading data frames (the same way as with RabbitMQ)
 */
public class CommandEndpoint extends ServerResource {

    public static String ENDPOINT = "/command";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    // logger
    final static Logger logger = LogManager.getLogger(CommandEndpoint.class.getName());

    /**
     * Dispatch and process POST request based on provided parameter
     * @param entity json
     * @return JSON
     */
    @Post
    public String processPost(Representation entity) throws IOException {
        counter.increment(ENDPOINT);

        try {
            // first access data consumer
            CommandConsumer consumer = new CommandConsumer();

            // process the message
            consumer.consume(entity.getText());

            // get execution status
            CommandConsumer.ExecutionStatus status = consumer.getStatus();

            // was successfully executed
            if (status.wasExecuted()) {

                // request restful container
                List<Object> list = Messenger.getInstance().retrieveRestfulContainer();

                // return it if it's not empty
                return (list != null && !list.isEmpty())? PrettyGson.toJson(list) : status.getMessage();
            } else {
                return status.getMessage();
            }

        } catch (Exception e) {
            return String.format("Error: %s", e.getMessage());
        }
    }
}