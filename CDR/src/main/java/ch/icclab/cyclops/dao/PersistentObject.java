package ch.icclab.cyclops.dao;
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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jooq.Field;
import org.jooq.Table;
import org.postgresql.util.PGobject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 08.05.17
 * Description: Interface for persisting objects
 */
public interface PersistentObject {
    /**
     * Get table of the object being persisted
     * @return table
     */
    Table<?> getTable();

    /**
     * Get fields of the object being persisted
     * NOTE: Order of fields needs to reflect getValues
     * @return collection of fields
     */
    Collection<? extends Field<?>> getFields();

    /**
     * Get values of the object being persisted
     * @return collection of values
     */
    Object[] getValues();

    /**
     * Transform PFObject field into Map
     * @param data field
     * @return Map, List<Type> or null
     */
    static Object PGObjectFieldToMap(Object data) {
        return PGObjectFieldToMap(data, null);
    }
    static Object PGObjectFieldToMap(Object data, Class clazz) {
        if (data == null) return null;

        String valueToParse;
        if (data instanceof PGobject) valueToParse = ((PGobject) data).getValue();
        else if (data instanceof String) valueToParse = (String) data;
        else return null;

        if (valueToParse != null && !valueToParse.isEmpty()) {
            if (clazz == null) clazz = Map.class;
            try {
                Object mapped = new Gson().fromJson(valueToParse, clazz);
                if (mapped != null) return mapped;
            } catch (Exception e) {
                List list = new Gson().fromJson(valueToParse, com.google.gson.reflect.TypeToken.getParameterized(List.class, clazz).getType());
                if (list != null && !list.isEmpty()) return list;
            }
        }

        // if something went wrong or was null
        return null;
    }
}