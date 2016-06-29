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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.CloudStackSettings;
import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Martin Skoviera
 * Created on: 19-Oct-15
 * Description: Creates API URL with provided command and signs it for later use
 */
public class CloudStackAuth {
    final static Logger logger = LogManager.getLogger(CloudStackAuth.class.getName());

    // access details for CloudStack API
    private static CloudStackSettings apiConnection;

    // command and parameters
    private CloudStackPuller.APICall APICall;

    /**
     * Simple constructor that will ask for connection details from Load configuration file
     */
    protected CloudStackAuth() {
        apiConnection = Loader.getSettings().getCloudStackSettings();
    }

    /**
     * Will return constructed URL for stored command and parameters
     *
     * @param APICall consisting command and parameters
     * @return signed URL
     */
    protected String getSignedURL(CloudStackPuller.APICall APICall) {
        this.APICall = APICall;

        return constructURL();
    }

    /**
     * Get CloudStack's Page size setting
     *
     * @return pagesize
     */
    protected Integer getPageSize() {
        return apiConnection.getCloudStackPageSize();
    }

    /**
     * Simple SHA1 implementation with Base64 encoding of message
     *
     * @param query header to be signed
     * @return signed string
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    private String signRequest(String query) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        logger.trace("Signing the CloudStack API query");

        Mac sha1_HMAC = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(apiConnection.getCloudStackSecretKey().getBytes(), "HmacSHA1");
        sha1_HMAC.init(secret);

        // now sign it and return Base64 representation
        String signature = Base64.encodeBase64String(sha1_HMAC.doFinal(query.getBytes()));

        return URLEncoder.encode(signature, "UTF-8");
    }

    /**
     * Perform construction of URL based on command provided, with some parameters as hashmap(key, value)
     *
     * @return constructed URL string
     */
    private String constructURL() {
        logger.trace("Constructing CloudStack API URL");

        // construct standard header
        Map<String, String> header = constructHeader();

        // now create url
        String queryString = Joiner.on("&").withKeyValueSeparator("=").join(header);

        try {
            // sign the normalised query string
            String signature = signRequest(queryString.toLowerCase());

            // add signature to our query string
            queryString = queryString.concat("&signature=".concat(signature));

            // return signed string
            return apiConnection.getCloudStackURL().concat("?".concat(queryString));

        } catch (NoSuchAlgorithmException e) {
            logger.error("Couldn't find encryption algorithm for signing CloudStack API URL");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            logger.error("Invalid Secret Key provided when trying to sign CloudStack API URL");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported Encoding used when trying to sign CloudStack API URL");
            e.printStackTrace();
        }

        // return empty string if something went wrong
        return "";
    }

    /**
     * Construct header with saved attributes
     *
     * @return header map
     */
    private Map<String, String> constructHeader() {
        logger.trace("Constructing CloudStack API Header");

        // we need a holder for our header request
        Map<String, String> header = null;

        // populate it with parameters if there are any
        header = new TreeMap<String, String>(APICall.getParameters());

        // now populate it with data
        header.put("apikey", apiConnection.getCloudStackAPIKey());
        header.put("command", APICall.getCommand());
        header.put("response", "json");

        return header;
    }
}
