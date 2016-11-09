package ch.icclab.cyclops.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.Response;
import ch.icclab.cyclops.model.OpenStackMeter;
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
import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 22/06/16.
 */

public class OpenStackMeterDownloader implements Callable {

    final static Logger logger = LogManager.getLogger(OpenStackMeterDownloader.class.getName());

    // what url we should fire an API
    private String url;

    // container for usage record response
    Response.UsageRecordResponse usageRecordResponse;

    /**
     * Simple constructor for this Threaded class
     *
     */
    public OpenStackMeterDownloader() {
        this.url = Loader.getSettings().getOpenStackSettings().getMeterUrl();
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

        OpenStackClient openStackClient = new OpenStackClient();
        token = openStackClient.generateToken();

        request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Content-Type", "application/json");
        headerValue.add("X-Auth-Token", token);

        clientResource.get(MediaType.APPLICATION_JSON);
        return clientResource.getResponseEntity().getText();
    }

    /**
     * Will perform request and pull meter data from OpenStack
     *
     */
    protected HashSet<OpenStackMeter> performRequest() {
        try {
            // first step is to pull data from OpenStack
            String data = pullData();
            Gson gson = new Gson();
            HashSet<OpenStackMeter> meters = new HashSet<>();
            //Create an array of OpenStackMeters
            OpenStackMeter[] metersArray = gson.fromJson(data, OpenStackMeter[].class);
            for(int i = 0; i<metersArray.length; i++){
                meters.add(metersArray[i]);
            }
            // return list of points
            return meters;

        } catch (Exception ignored) {
            logger.error("Couldn't pull Usage Records from OpenStack ");
            return null;
        }
    }

    @Override
    public Object call() throws Exception {
        return performRequest();
    }
}
