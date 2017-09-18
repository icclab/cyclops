package ch.icclab.cyclops.consume.command.generation.runner;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import ch.icclab.cyclops.consume.command.AbstractGeneration;
import ch.icclab.cyclops.consume.command.generation.usage.OpenStackObjectActiveUsage;
import ch.icclab.cyclops.consume.command.generation.usage.Usage;
import ch.icclab.cyclops.util.loggers.CommandLogger;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.json.*;
import javax.json.JsonValue.ValueType;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: S3 server client
 */
public class S3UsageGeneration extends AbstractGeneration {

    @Override
    public Class getUsageFormat(){ return OpenStackObjectActiveUsage.class; }

    @Override
    public ArrayList<Usage> generateUsageRecords() {
        ArrayList<Usage> listOfUsages = new ArrayList<>();
        ArrayList<Usage> listOfValidUsages = new ArrayList<>();
        String keyId = settings.getS3serverCredentials().getS3KeyId();
        String key = settings.getS3serverCredentials().getS3Key();
        String endpoint = settings.getS3serverCredentials().getEndpoint();
        try {
            listOfUsages = S3UsageGeneration.getStats(keyId, key, endpoint);
        } catch (Exception e) {
            String message = String.format("S3 data can not be fetched %s",e);
            status.setClientError(message);
            CommandLogger.log(message);
        }
        for (Usage usage: listOfUsages) {
            OpenStackObjectActiveUsage usageTransformed = (OpenStackObjectActiveUsage) usage;
            if (second_step !=0) {
                Boolean lastIteration = false;
                long currentTime;
                long lastTime = time_from;
                do {
                    OpenStackObjectActiveUsage singleUsage = usageTransformed.clone() ;
                    currentTime = lastTime + second_step;
                    if (currentTime >= time_to){
                        currentTime = time_to;
                        lastIteration = true;
                    }
                    singleUsage.setTime(currentTime);
                    singleUsage.setUsage((currentTime - lastTime)/1000);
                    listOfValidUsages.add(singleUsage);
                    lastTime = currentTime;
                } while (!lastIteration);
            }else {
                usageTransformed.setTime(time_to);
                usageTransformed.setUsage((double) (time_to - time_from)/1000);
                listOfValidUsages.add(usageTransformed);
            }
        }
        return listOfValidUsages;
    }

    /**
     *
     * http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html
     * http://docs.ceph.com/docs/master/radosgw/s3/authentication/
     *
     * @param keyId
     * @param key
     * @param endpoint
     * @return
     * @throws Exception
     */
    public static ArrayList<Usage> getStats(String keyId, String key, String endpoint) throws Exception {
        ArrayList<Usage> buckets = new ArrayList<>();

        // Init key
        Mac mac = Mac.getInstance("HmacSHA1");
        byte[] keyBytes = key.getBytes("UTF8");
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        mac.init(signingKey);

        // S3 timestamp pattern.
        String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
        SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Data needed for signature
        String method = "GET";
        String contentMD5 = "";
        String contentType = "";
        String date = df.format(new Date()) + "GMT";
        String resource = "/admin/bucket";

        // Generate signature
        StringBuffer buf = new StringBuffer();
        buf.append(method).append("\n");
        buf.append(contentMD5).append("\n");
        buf.append(contentType).append("\n");
        buf.append(date).append("\n");
        buf.append(resource);
        byte[] signBytes = mac.doFinal(buf.toString().getBytes("UTF8"));
        Base64 encoder = new Base64();
        String signature = new String(encoder.encode(signBytes));

        // Connection to s3.amazonaws.com
        HttpURLConnection httpConn = null;
        URL url = new URL("https", endpoint, 443, resource + "?stats=true&format=json");
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoInput(true);
        httpConn.setDoOutput(false);
        httpConn.setRequestMethod(method);
        httpConn.setRequestProperty("Date", date);
        httpConn.setRequestProperty("Content-Length", "0");
        String AWSAuth = "AWS " + keyId + ":" + signature;
        httpConn.setRequestProperty("Authorization", AWSAuth);

        // Send the HTTP PUT request.
        BufferedInputStream in = new BufferedInputStream(httpConn.getInputStream());
        String region = settings.getOpenstackSettings().getOpenstackDefaultRegion();

        int ret = httpConn.getResponseCode();
        if (ret >= 200 && ret <= 400) {
            JsonReader reader = Json.createReader(in);
            JsonArray json = (JsonArray)reader.read();
            for(JsonValue v : json){
                if(v.getValueType() == ValueType.OBJECT){
                    JsonObject bucket = (JsonObject) v;
                    if(bucket.containsKey("usage")){
                        JsonObject usage = (JsonObject) bucket.get("usage");
                        if(usage.containsKey("rgw.main")){
                            JsonObject main = (JsonObject) usage.get("rgw.main");
                            double gb = main.getJsonNumber("size_kb_actual").longValue()/1000/1000;
                            Usage b = new OpenStackObjectActiveUsage(bucket.getString("owner"),
                                    bucket.getString("id"), bucket.getString("bucket"),gb, region);
                            buckets.add(b);
                        }

                    }
                }else{
                    //System.out.println(v);
                }
            }
        }
        httpConn.disconnect();
        return buckets;
    }
}

