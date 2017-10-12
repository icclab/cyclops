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
import ch.icclab.cyclops.model.OpenStackMeter;
import ch.icclab.cyclops.model.OpenStackUsageData;
import ch.icclab.cyclops.model.ceilometerMeasurements.AbstractOpenStackCeilometerUsage;
import ch.icclab.cyclops.persistence.CumulativeMeterUsage;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.util.CachedCumulativeUsage;
import ch.icclab.cyclops.util.Constant;
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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
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

    private HashMap<String, CumulativeMeterUsage> cumulativeMap;

    private HibernateClient hibernateClient;

    // container for usage record response
    Response.UsageRecordResponse usageRecordResponse;

    /**
     * Simple constructor for this Threaded class
     *
     * @param url that will be used for API call
     */
    public OpenStackUsageDownloader(String url) {
        this.openStackClient = new OpenStackClient();
        this.url = url;
        this.cumulativeMap = CachedCumulativeUsage.getCachedCumulativeUsage();
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
        headerValue = new Series<>(Header.class);
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
            hibernateClient = HibernateClient.getInstance();
            // first step is to pull data from OpenStack
            List<Object> genericData = new ArrayList<>();
            if (meter != null) {
                String data = pullData();
                Gson gson = new Gson();
                OpenStackUsageData[] openStackUsageData = gson.fromJson(data, OpenStackUsageData[].class);
                for (int i = 0; i < openStackUsageData.length; i++) {
                    // Construct the pojo class and add it to the generic data list
                    Constructor constructor = Class.forName(Constant.METER_NAMES.get(meter.getName())).getConstructor(OpenStackUsageData.class, OpenStackMeter.class);
                    AbstractOpenStackCeilometerUsage openStackUsage = (AbstractOpenStackCeilometerUsage) constructor.newInstance(openStackUsageData[i], meter);
                    if (meter.getType().equals(Constant.CEILOMETER_CUMULATIVE_METER)) {
                        // Get the older usage (if exists) and compute the cumulative meter out of the two measurements
                        String usageKey = getUsageKey(openStackUsageData[i], meter);
                        CumulativeMeterUsage cumulativeMeterUsage = new CumulativeMeterUsage(openStackUsage, usageKey);
                        updateToExistingId(cumulativeMeterUsage);
                        openStackUsage.setUsage(getCumulativeUsage(openStackUsageData[i].getAvg(), usageKey));
                        // Persist the cumulativeUsage in hibernate
                        hibernateClient.persistObject(cumulativeMeterUsage);
                        // Get the ID from the persisted object and store it in memory (HashMap) linked to the latest usage
                        cumulativeMap.put(usageKey, cumulativeMeterUsage);
                    }
                    genericData.add(openStackUsage);
                }
            }
            // return list of points
            return genericData;

        } catch (Exception ignored) {
            logger.error("Couldn't pull Usage Records from OpenStack ");
            return null;
        }
    }

    private void updateToExistingId(CumulativeMeterUsage cumulativeMeterUsage) {
        hibernateClient = HibernateClient.getInstance();
        ArrayList<CumulativeMeterUsage> cumulativeDataList = (ArrayList<CumulativeMeterUsage>) hibernateClient.executeQuery("FROM CumulativeMeterUsage WHERE usageKey='" + cumulativeMeterUsage.getUsageKey() + "'");
        if(!cumulativeDataList.isEmpty()){
            for(CumulativeMeterUsage usageData : cumulativeDataList){
                cumulativeMeterUsage.setId(usageData.getId());
            }
        }
    }

    @Override
    public Object call() throws Exception {
        return performRequest(new OpenStackMeter());
    }

    private Double getCumulativeUsage(Double measurementUsage, String usageKey) {
        Double result;
        if (cumulativeMap.containsKey(usageKey))
            result = measurementUsage - cumulativeMap.get(usageKey).getUsageCounter();
        else {
            hibernateClient = HibernateClient.getInstance();
            ArrayList<CumulativeMeterUsage> cumulativeDataList = (ArrayList<CumulativeMeterUsage>) hibernateClient.executeQuery("FROM CumulativeMeterUsage WHERE usageKey='" + usageKey + "'");
            if (!cumulativeDataList.isEmpty()) {
                for (CumulativeMeterUsage data : cumulativeDataList)
                    cumulativeMap.put(data.getUsageKey(), data);
                result = measurementUsage - cumulativeMap.get(usageKey).getUsageCounter();
                if(result<=0){
                    result = measurementUsage;
                }
            }
            else
            //TODO: Review
                result = 0.0;
        }
        return result;
    }

    private String getUsageKey(OpenStackUsageData openStackUsageData, OpenStackMeter openStackMeter) {
        String measurementName = openStackMeter.getName();
        String resourceId = (String) openStackUsageData.getGroupby().get("resource_id");
        String projectId = (String) openStackUsageData.getGroupby().get("project_id");
        String userId = (String) openStackUsageData.getGroupby().get("user_id");
        return measurementName.concat(resourceId).concat(projectId).concat(userId);
    }
}
