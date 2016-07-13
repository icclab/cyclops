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

import ch.icclab.cyclops.consume.data.DataConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.util.APICallCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.IOException;

/**
 * Author: Skoviera
 * Created: 08/07/16
 * Description: Handle uploading data frames (the same way as with RabbitMQ)
 */
public class DataEndpoint extends ServerResource {

    public static String ENDPOINT = "/data";

    // used for accessing DataConsumer
    private PublisherCredentials publisher;
    private String defaultName;

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    // logger
    final static Logger logger = LogManager.getLogger(DataEndpoint.class.getName());

    public DataEndpoint() {
        Settings settings = Loader.getSettings();
        this.defaultName = settings.getInfluxDBCredentials().getInfluxDBDefaultMeasurement();
        this.publisher = settings.getPublisherCredentials();
    }

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
            DataConsumer consumer = new DataConsumer(defaultName, publisher);

            // process the message
            consumer.consume(entity.getText());

            // get stats
            Long valid = consumer.getNumberOfValidRecords();


            return (valid > 0) ? String.format("%d processed", valid) : "Invalid JSON";

        } catch (Exception e) {
            return String.format("Error: %s", e.getMessage());
        }
    }
}