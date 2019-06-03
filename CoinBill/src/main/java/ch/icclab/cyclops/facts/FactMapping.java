package ch.icclab.cyclops.facts;
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

import ch.icclab.cyclops.util.FindClass;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.gsonfire.GsonFireBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 07/03/16
 * Description: Helper class for automatic GSON class selection
 */
public class FactMapping {
    private static final Gson gson = new GsonFireBuilder()
        .registerTypeSelector(MappedFact.class, jsonElement -> {
            try {
                JsonObject object = jsonElement.getAsJsonObject();

                if (object.has(Usage.USAGE_FIELD)) return Usage.class;
                else if (object.has(Charge.CHARGE_FIELD)) return Charge.class;
                else if (object.has(TypedFact.TYPE_CONSTANT)) return FindClass.inPackage(TypedFact.class.getPackage(), object.get(TypedFact.TYPE_CONSTANT).getAsString());
                else return null;
            } catch (Exception e) {
                return null;
            }
        }).createGson();

    /**
     * Map object to provided class
     * @param json string
     * @return list of mapped objects or null
     */
    public static List<MappedFact> fromJson(String json) {
        try {
            // try to map it as an object
            MappedFact obj = gson.fromJson(json, MappedFact.class);

            // make sure not to include blank mapped fact
            return (obj != null && !obj.getClass().equals(MappedFact.class))? Collections.singletonList(obj): null;
        } catch (Exception e) {
            try {
                // but it could be an array
                Type type = new TypeToken<ArrayList<MappedFact>>(){}.getType();
                List<MappedFact> list = gson.fromJson(json, type);

                if (list == null) return null;
                else {
                    List<MappedFact> valid = new ArrayList<>();

                    // remove blank mapped facts
                    for (MappedFact fact: list) {
                        if (!fact.getClass().equals(MappedFact.class)) {
                            valid.add(fact);
                        }
                    }

                    // never return empty list
                    return (!valid.isEmpty())? valid : null;
                }
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
