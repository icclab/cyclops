package ch.icclab.cyclops.timeseries;
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

import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 31/08/16
 * Description: Shared InfluxDB session
 */
public class SharedInfluxDBSession {
    private static InfluxDBClient session;

    public static InfluxDBClient getSession() {
        if (session == null) {
            session = new InfluxDBClient();
            session.configureBatchOnSinglePoints(2000, 100, TimeUnit.MILLISECONDS);
        }

        return session;
    }
}
