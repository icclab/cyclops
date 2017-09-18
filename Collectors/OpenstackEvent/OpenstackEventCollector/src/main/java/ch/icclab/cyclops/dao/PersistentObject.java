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

}