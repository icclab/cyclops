/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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
package ch.icclab.cyclops.consume.data.mapping.usage;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the OpenStackImageActiveUsage response
 */
public class OpenStackImageActiveUsage extends OpenStackUsage {
    public OpenStackImageActiveUsage(Long time, String account, String resourceId, Double usage, String imageId){
        this.account = account;
        this.usage = usage;
        this.time = time;
        this.metadata = new OpenStackImageActiveUsage.ImageMetadata(resourceId, imageId);
    }

    private String unit = "sec";
    private String _class = getClass().getSimpleName();
    private OpenStackImageActiveUsage.ImageMetadata metadata;

    private class ImageMetadata {
        private String  resourceId;
        private String imageId;
        ImageMetadata(String resourceId, String imageId){
            this.resourceId = resourceId;
            this.imageId = imageId;
        }
    }
}

