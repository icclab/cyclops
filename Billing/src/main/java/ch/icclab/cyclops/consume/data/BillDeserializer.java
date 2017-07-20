package ch.icclab.cyclops.consume.data;
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

import ch.icclab.cyclops.dao.Bill;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.gsonfire.PreProcessor;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 12.05.17
 * Description: Handle edge cases for Bill JSON deserialization
 */
public class BillDeserializer<T> implements PreProcessor <T> {
    @Override
    public void preDeserialize(Class<? extends T> clazz, JsonElement jsonElement, Gson gson) {

        // valid JSON object
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject root = jsonElement.getAsJsonObject();

            // map data to string so it can be persisted as jsonb
            if (root.has(Bill.DATA_FIELD.getName())) {
                root.addProperty(Bill.DATA_FIELD.getName(), new Gson().toJson(root.get(Bill.DATA_FIELD.getName())));
            }
        }
    }
}

