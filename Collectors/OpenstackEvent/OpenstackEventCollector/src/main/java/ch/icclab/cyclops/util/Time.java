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

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * Author: Martin Skoviera
 * Created on: 22-Oct-15
 * Description: Time parser for CloudStack's needs
 */
public class Time {

    public static Long fromNovaTimeToMills(String time){
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").withZoneUTC();
        DateTime date = format.parseDateTime(time);

        return date.getMillis();
    }

    public static Long fromOpenstackTimeToMills(String time){
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd' 'HH:mm:ss.SSSSSS").withZoneUTC();
        DateTime date = format.parseDateTime(time);

        return date.getMillis();
    }
}
