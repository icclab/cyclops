package ch.icclab.cyclops.consume.data;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 13/05/16
 * Description: Fields for Data Mapping
 */
public interface DataMapping {

    String FIELD_FOR_MAPPING = "_class";

    /**
     * What field in JSON holds LONG timestamp
     * @return string or null
     */
    String getTimeField();

    /**
     * What TimeUnit should be used
     * @return TimeUnit or null (seconds will be used)
     */
    TimeUnit getTimeUnit();

    /**
     * What JSON FIELDS are to be stored as TAGS
     * @return list of strings or null
     */
    List<String> getTagNames();

    /**
     * Pre-process, update or change incoming fields and values
     * This method is called for you on the level of original Map (parsed JSON)
     * @param original Map representation of the incoming object
     * @return preprocessed or null (original will be used)
     */
    Map preProcess(Map original);

    /**
     * Should response be automatically published?
     * @return true or false
     */
    Boolean shouldPublish();

    /**
     * Should response be broadcast or routed?
     * @return true or false
     */
    Boolean doNotBroadcastButRoute();
}
