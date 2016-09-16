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
package ch.icclab.cyclops.util;

import ch.icclab.cyclops.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.gsonfire.GsonFireBuilder;
import io.gsonfire.TypeSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Description: Helper class for automatic GSON class selection
 */
public class GsonMapping {

    final static Logger logger = LogManager.getLogger(GsonMapping.class.getName());

    // automatic selection of CloudStack usage type records
    private static final GsonFireBuilder builder = new GsonFireBuilder()
            .registerTypeSelector(UsageData.class, new TypeSelector<UsageData>() {
                @Override
                public Class<? extends UsageData> getClassForElement(JsonElement readElement) {
                    int usagetype = readElement.getAsJsonObject().get(Constant.CLOUDSTACK_USAGE_TYPE).getAsInt();

                    // now return corresponding classes
                    switch (usagetype) {
                        case 1:
                            return VMRunning.class;
                        case 2:
                            return VMAllocated.class;
                        case 3:
                            return IP.class;
                        case 4:
                            return NetworkSent.class;
                        case 5:
                            return NetworkReceived.class;
                        case 6:
                            return Volume.class;
                        case 7:
                            return Template.class;
                        case 8:
                            return ISO.class;
                        case 9:
                            return Snapshot.class;
                        case 11:
                            return LoadBalancer.class;
                        case 12:
                            return PortForwarding.class;
                        case 13:
                            return NetworkOffering.class;
                        case 14:
                            return VPNUser.class;
                        default:
                            //returning null will trigger Gson's default behavior
                            logger.error("Unknown class in received JSON - using default GSON behaviour");
                            return null;
                    }
                }
            });


    public static Gson getGson() {
        return builder.createGson();
    }
}
