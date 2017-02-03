package ch.icclab.cyclops.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.OpenStackCeilometerResource;
import ch.icclab.cyclops.model.OpenStackMeter;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.util.Constant;
import ch.icclab.cyclops.util.DateInterval;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
 * Created by Manu Perez on 21/06/16.
 */

public class OpenStackPuller {
    final static Logger logger = LogManager.getLogger(OpenStackPuller.class.getName());

    private int pageSize = Loader.getSettings().getServerSettings().getPageSize();

    /**
     * Get data from OpenStack and parse it into list of UsageData objects (with pagination support)
     *
     * @return whether operation was successful
     */
    public Boolean pullUsageRecords() {
        logger.trace("Trying to pull Custom Usage Records from Vanilla OpenStack");

        Boolean status = pull();

        if (status) {
            logger.trace("Usage Records successfully pulled from Vanilla OpenStack");
        } else {
            logger.error("Couldn't pull Usage Records from Vanilla OpenStack, consult logs");
        }

        return status;
    }

    /**
     * Pull, retrieve and parse UsageRecords from OpenStack
     *
     * @return container with all retrieved points
     */
    private Boolean pull() {
        // whether to start from epoch or last commit
        DateInterval dates = new DateInterval();
        String url = "";
        OpenStackMeterDownloader openStackMeterDownloader = new OpenStackMeterDownloader();
        HashSet<OpenStackMeter> meters = openStackMeterDownloader.performRequest();
        ArrayList<String> supportedMeters = new ArrayList<>(Arrays.asList(Loader.getSettings().getOpenStackSettings().getSupportedMeterList().split(",")));

        List<Object> records = null;
        // build the url for requesting usage data from each meter
        for (OpenStackMeter meter : meters) {
            url = generateUsageUrl(dates, meter);
            OpenStackUsageDownloader openStackUsageDownloader = new OpenStackUsageDownloader(url);
            // Check if the supported meter selection is empty
            if (supportedMeters != null)
                // Check if the meter is supported
                if (supportedMeters.contains(Constant.FULL_METER_SELECTION) || supportedMeters.contains(meter.getName()))
                    // first run has to be manual (not threaded)
                    if (records == null) {
                        records = openStackUsageDownloader.performRequest(meter);
                    } else {
                        List<Object> newRecords = openStackUsageDownloader.performRequest(meter);
                        if (newRecords != null)
                            records.addAll(newRecords);
                    }
        }
        // only if we have valid list
        if (records != null) {
            // here is the point when everything is downloaded, so lets save first page and then the rest
            broadcastRecords(records);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Broadcast list of items on RabbitMQ
     *
     * @param list to be broadcast
     */
    private void broadcastRecords(List<Object> list) {
        for (Object item : list) {
            Messenger.getInstance().broadcast(item);
        }
    }

    private String generateUsageUrl(DateInterval dates, OpenStackMeter meter) {
        String metername = meter.getName();
        String type = meter.getType();
        String url = Loader.getSettings().getOpenStackSettings().getCeilometerUrl();

        String from = dates.getFromDate();
        String to = dates.getToDate();
        if (from != "" && to != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=gt&q.value=" + from + "&q.field=timestamp&q.op=lt&q.value=" + to + "&groupby=user_id&groupby=project_id&groupby=resource_id";
        else if (from != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=gt&q.value=" + from + "&groupby=user_id&groupby=project_id&groupby=resource_id";
        else if (to != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=lt&q.value=" + to + "&groupby=user_id&groupby=project_id&groupby=resource_id";

        return url;
    }

    public String getResourceIdName(String resourceId) {
        try {
            String url = Loader.getSettings().getOpenStackSettings().getCeilometerUrl();
            url = url + "resources/" + resourceId;

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
            String data = clientResource.getResponseEntity().getText();

            Gson gson = new Gson();
            OpenStackCeilometerResource resource = gson.fromJson(data, OpenStackCeilometerResource.class);
            return (String) resource.getMetadata().get("display_name");
        } catch (Exception e) {
            return "";
        }
    }

}
