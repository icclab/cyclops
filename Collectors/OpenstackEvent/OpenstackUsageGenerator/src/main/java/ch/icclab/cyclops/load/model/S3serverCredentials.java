package ch.icclab.cyclops.load.model;
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

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: S3 credentials
 */
public class S3serverCredentials {

    private String s3KeyId;
    private String s3Key;
    private String endpoint;

    public String getS3KeyId() { return s3KeyId; }

    public void setS3KeyId(String s3KeyId) { this.s3KeyId = s3KeyId; }

    public String getS3Key() { return s3Key; }

    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getEndpoint() { return endpoint; }

    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
}
