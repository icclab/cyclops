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

    /**
     * Will compute number of milliseconds from epoch to startDate
     *
     * @param time as string
     * @return milliseconds since epoch
     */
    public static long getMilisForTime(String time) {
        // first we have to get rid of 'T', as we need just T
        String isoFormat = time.replace("'", "");

        // then we have to create proper formatter
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                .withLocale(Locale.ROOT)
                .withChronology(ISOChronology.getInstanceUTC());

        // and now parse it
        DateTime dt = formatter.parseDateTime(isoFormat);

        return dt.getMillis();
    }

    /**
     * This method computes the date of String time (with support for 'T' and time zones)
     *
     * @param full
     * @return UTC DateTime object
     */
    public static DateTime getDateForTime(String full) {
        String day = (full.contains("T")) ? full.substring(0, full.indexOf("T")) : full;

        // convert it to time object
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withLocale(Locale.ROOT)
                .withChronology(ISOChronology.getInstanceUTC());

        return formatter.parseDateTime(day);
    }

    /**
     * Normalise date in string format
     *
     * @param original string
     * @return normalised string
     */
    public static String normaliseString(String original) {
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
                    .withLocale(Locale.ROOT)
                    .withChronology(ISOChronology.getInstanceUTC());

            // and now parse it
            DateTime dt = formatter.parseDateTime(original);

            return dt.toString();

        } catch (Exception ignored) {
            return original;
        }
    }
}
