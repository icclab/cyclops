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

import com.google.gson.Gson;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 18.05.17
 * Description: HTTP Status mapping
 */
public class HTTPOutput {
    private transient Status status;
    private String description;
    private Object result;
    private int code;

    public static MediaType MEDIA_TYPE = MediaType.APPLICATION_JSON;

    public HTTPOutput(Status status, String description) {
        this.status = status;
        this.description = description;
        this.code = status.getCode();
    }

    public HTTPOutput(String description, Object result) {
        this.status = Status.SUCCESS_OK;
        this.description = description;
        this.code = status.getCode();
        this.result = result;
    }

    public int getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public Object getResult() {
        return result;
    }
    public Status getStatus() {
        return status;
    }

    /**
     * Fill response with JSON status and description
     * @param response to be used
     * @return Response or null
     */
    public Response prepareResponse(Response response) {
        return prepareResponse(response, true);
    }
    public Response prepareResponse(Response response, boolean withEnvelope) {
        if (response == null || status == null || description == null) return null;

        // fill status with description and make sure entity is of type JSON
        response.setStatus(status, description);

        Gson gson = new Gson();

        if (!withEnvelope && result != null) response.setEntity(gson.toJson(result), MEDIA_TYPE);
        else response.setEntity(gson.toJson(this), MEDIA_TYPE);

        return response;
    }

    @Override
    public String toString() {
        return String.format("%d - %s", code, description);
    }
}
