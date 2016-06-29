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
package ch.icclab.cyclops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 19-Oct-15
 * Description: POJO object for CloudStack JSON response
 */
public class Response {

    // response for listUsageRecords API call
    private UsageRecordResponse listusagerecordsresponse;

    /////////////////////////////
    // Getters and Setters
    public void setListusagerecordsresponse(UsageRecordResponse listusagerecordsresponse) {
        this.listusagerecordsresponse = listusagerecordsresponse;
    }

    /**
     * Access parsed Usage Records
     *
     * @return
     */
    public UsageRecordResponse getUsageRecordsResponse() {
        if (listusagerecordsresponse != null) {
            return listusagerecordsresponse;
        } else {
            return null;
        }
    }

    /**
     * Class for UsageResponse JSON response
     */
    public class UsageRecordResponse {
        // number of received responses
        private Integer count;
        // array of usage records
        private List<UsageData> usagerecord;

        /**
         * This method will go over every usage record and return DB Point representation
         *
         * @return list of points for this usagerecord
         */
        public List<Object> getAllPoints() {
            List<Object> points = new ArrayList<>();

            // in case we are working with vanilla CloudStack
            if (usagerecord != null) {
                points.addAll(usagerecord);
            }

            // normalise everything
            for (Object obj: points) {
                ((UsageData) obj).prepareForSending();
            }

            return points;
        }

        /////////////////////////////
        // Getters and Setters

        public Integer getCount() {
            if (count == null) {
                return 0;
            } else {
                return count;
            }
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public void setUsagerecord(List<UsageData> usagerecord) {
            this.usagerecord = usagerecord;
        }
    }
}
