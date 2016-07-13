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

import ch.icclab.cyclops.load.model.Response;
import ch.icclab.cyclops.model.OpenStackCeilometerUsage;
import ch.icclab.cyclops.model.OpenStackMeter;
import ch.icclab.cyclops.model.OpenStackUsageData;
import ch.icclab.cyclops.util.OpenStackClient;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Author: Manu Perez
 * Created on: 21-June-16
 * Description: Thread safe class that will download selected page of Samples from OpenStack and add it to thread safe list
 */
public class OpenStackUsageDownloader implements Callable {

    final static Logger logger = LogManager.getLogger(OpenStackUsageDownloader.class.getName());

    // what url we should fire an API
    private String url;

    private OpenStackClient openStackClient;

    // container for usage record response
    Response.UsageRecordResponse usageRecordResponse;

    /**
     * Simple constructor for this Threaded class
     *
     * @param url that will be used for API call
     */
    public OpenStackUsageDownloader(String url) {
        openStackClient = new OpenStackClient();
        this.url = url;
    }

    /**
     * Pulls data from OpenStack API
     *
     * @return JSON string
     * @throws IOException
     */
    private String pullData() throws IOException {
        Series<Header> headerValue;

        ClientResource clientResource = new ClientResource(url);
        headerValue = new Series<Header>(Header.class);
        Request request = clientResource.getRequest();
        String token;

        token = openStackClient.generateToken();

        request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Content-Type", "application/json");
        headerValue.add("X-Auth-Token", token);

        clientResource.get(MediaType.APPLICATION_JSON);
        return clientResource.getResponseEntity().getText();
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
        Gson gson = new Gson();
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
     * Will perform request and pull data from OpenStack
     */
    protected List<Object> performRequest(OpenStackMeter meter) {
        try {
            // first step is to pull data from OpenStack
            List<Object> genericData = new ArrayList<>();
            if(meter != null) {
            String data = pullData();
            Gson gson = new Gson();
            OpenStackUsageData[] openStackUsageData = gson.fromJson(data, OpenStackUsageData[].class);
                for (int i = 0; i < openStackUsageData.length; i++) {
                    genericData.add(new OpenStackCeilometerUsage(openStackUsageData[i], meter));
                }
            }
            // return list of points
            return genericData;

        } catch (Exception ignored) {
            logger.error("Couldn't pull Usage Records from OpenStack ");
            return null;
        }
    }

    @Override
    public Object call() throws Exception {
        return performRequest(new OpenStackMeter());
    }
}
