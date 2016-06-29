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
package ch.icclab.cyclops.client;

import ch.icclab.cyclops.model.Response;
import ch.icclab.cyclops.util.GsonMapping;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Author: Martin Skoviera
 * Created on: 22-Oct-15
 * Description: Thread safe class that will download selected page of Usage Records from CloudStack and add it to thread safe list
 */
public class CloudStackDownloader implements Callable {

    final static Logger logger = LogManager.getLogger(CloudStackDownloader.class.getName());

    // what url we should fire an API
    private String url;

    // container for usage record response
    Response.UsageRecordResponse usageRecordResponse;

    /**
     * Simple constructor for this Threaded class
     *
     * @param url that will be used for API call
     */
    public CloudStackDownloader(String url) {
        this.url = url;
    }

    /**
     * Pulls data from CloudStack API
     *
     * @return JSON string
     * @throws IOException
     */
    private String pullData() throws IOException {
        logger.trace("Starting to pull data from provided URL");

        // create connection
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Request req = cr.getRequest();

        // now header
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");

        // fire it up
        cr.get(MediaType.APPLICATION_JSON);
        Representation output = cr.getResponseEntity();

        logger.trace("Successfully pulled data from provided URL");

        // and return response data
        return output.getText();
    }

    /**
     * Parse listUsageRecords response
     *
     * @param data received response
     * @return list of parsed objects
     */
    private Response.UsageRecordResponse parseListUsageRecordsResponse(String data) {
        logger.trace("Parsing received Usage Records");

        // parse received data
        Gson gson = GsonMapping.getGson();
        Response response = gson.fromJson(data, Response.class);

        logger.trace("Usage records successfully parsed");

        return response.getUsageRecordsResponse();
    }

    /**
     * Asks for number of usage records from usageRecordResponse
     */
    protected Integer getCount() {
        return usageRecordResponse.getCount();
    }

    /**
     * Will perform request and pull data from CloudStack
     *
     * @return list of points that will be saved into DB
     */
    protected List<Object> performRequest() {
        try {
            // first step is to pull data from CloudStack
            String data = pullData();

            // time to parse received data
            usageRecordResponse = parseListUsageRecordsResponse(data);

            // return list of points
            return usageRecordResponse.getAllPoints();

        } catch (Exception ignored) {
            logger.error("Couldn't pull Usage Records from CloudStack ");
            return null;
        }
    }

    @Override
    public Object call() throws Exception {
        return performRequest();
    }
}
