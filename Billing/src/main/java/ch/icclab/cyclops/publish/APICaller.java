package ch.icclab.cyclops.publish;
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
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 29/07/16
 * Description: Call API endpoint
 */
public class APICaller {

    public class Response {
        private String object;
        private int status;

        public Response(HttpEntity obj, int stat) throws Exception {
            if (obj != null) object = IOUtils.toString(obj.getContent());
            status = stat;
        }

        public Response(ResponseBody obj, int stat) throws Exception {
            if (obj != null) object = obj.toString();
            status = stat;
        }

        public int getStatus() {
            return status;
        }

        public String getAsString() throws Exception {
            return object;
        }

        public List getAsList() throws Exception {
            return new Gson().fromJson(object, List.class);
        }

        public <T> List<T> getAsListOfThisType(Class<T> clazz) throws Exception {
            return new Gson().fromJson(object, TypeToken.getParameterized(List.class, clazz).getType());
        }

        public Map getAsMap() throws Exception {
            return new Gson().fromJson(object, Map.class);
        }

        public <T> T getAsThisType(Class<T> clazz) throws Exception {
            return new Gson().fromJson(object, clazz);
        }
    }

    /**
     * Perform POST query and return Response
     * @param endpoint to be called
     * @param object to be passed
     * @return Response object
     * @throws Exception
     */
    public APICaller.Response post(URL endpoint, Object object) throws Exception {
        // prepare connection
        HttpClient client = HttpClientBuilder.create().build();

        // create request
        HttpPost request = new HttpPost(endpoint.toURI());
        StringEntity entity = new StringEntity(new Gson().toJson(object));
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        request.setEntity(entity);

        // execute response
        HttpResponse response = client.execute(request);
        return new Response(response.getEntity(), response.getStatusLine().getStatusCode());
    }

    public APICaller.Response postXML(URL endpoint, String object) throws Exception {
        /*
        // prepare connection
        HttpClient client = HttpClientBuilder.create().build();

        // create request
        HttpPost request = new HttpPost(endpoint.toURI());
        StringEntity entity = new StringEntity(object);
        request.addHeader("Accept", "application/xml");
        request.addHeader("Content-Type", "application/xml");
        request.setEntity(entity);

        // execute response
        HttpResponse response = client.execute(request);
        return new Response(response.getEntity(), response.getStatusLine().getStatusCode());
        */

        // SSL settings
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS).allEnabledCipherSuites().build();

        // prepare connection
        OkHttpClient client = new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(spec)).build();

        // prepare header and body
        MediaType XML = MediaType.parse("application/xml; charset=utf-8");
        RequestBody body = RequestBody.create(XML, object);

        // build request
        Request request = new Request.Builder().url(endpoint).post(body).build();

        // call it
        okhttp3.Response ret = client.newCall(request).execute();

        return new Response(ret.body(), ret.code());
    }

    /**
     * Perform GET query and return Response
     * @param endpoint to be called
     * @return Response object
     * @throws Exception
     */
    public APICaller.Response get(URL endpoint) throws Exception {
        // prepare connection
        HttpClient client = HttpClientBuilder.create().build();

        // create request
        HttpGet request = new HttpGet(endpoint.toURI());
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");

        // execute response
        HttpResponse response = client.execute(request);
        return new Response(response.getEntity(), response.getStatusLine().getStatusCode());
    }
}
