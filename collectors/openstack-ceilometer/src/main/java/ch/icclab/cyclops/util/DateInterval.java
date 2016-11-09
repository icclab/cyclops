package ch.icclab.cyclops.util;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p/>
 * Created by Manu Perez on 14/09/16.
 */

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is being used to generate interval either from last point or epoch
 */
public class DateInterval {

    final static Logger logger = LogManager.getLogger(DateInterval.class.getName());
    private String fromDate;
    private String toDate;

    public DateInterval() {
        DateTime from = whenWasLastPull();
        fromDate = from.toString("yyyy-MM-dd'T'HH:mm:ssZ");
        toDate = new DateTime(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZ");
        if (fromDate.contains("+"))
            fromDate = fromDate.substring(0, fromDate.indexOf("+"));
        if (toDate.contains("+"))
            toDate = toDate.substring(0, toDate.indexOf("+"));
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public Long getFromLong() {
        Date from = new Date(fromDate);
        return from.getTime();
    }

    public Long getToLong() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date to;
        try {
            to = simpleDateFormat.parse(toDate);
            return to.getTime();
        } catch (ParseException e) {
            logger.error("Error while generating the LasPullDate: " + e.getMessage());
            throw new Exception("Error while generating the LastPullDate: " + e.getMessage());
        }
    }


    /**
     * Will determine when was the last entry point (pull from Ceilometer), or even if there was any
     *
     * @return date object of the last commit, or epoch if there was none
     */

    private DateTime whenWasLastPull() {
        DateTime last;

        LatestPullORM pull = (LatestPullORM) HibernateClient.getInstance().getObject(LatestPullORM.class, 1l);
        if (pull == null) {
            last = new DateTime(0);
        } else {
            last = new DateTime(pull.getTimeStamp());
        }

        logger.trace("Getting the last pull date " + last.toString());

        // get date specified by admin
        String date = Loader.getSettings().getOpenStackSettings().getFirstImport();
        if (date != null && !date.isEmpty()) {
            try {
                logger.trace("Admin provided us with import date preference " + date);
                DateTime selection = Time.getDateForTime(date);

                // if we are first time starting and having Epoch, change it to admin's selection
                // otherwise skip admin's selection and continue from the last DB entry time
                if (last.getMillis() == 0) {
                    logger.debug("Setting first import date as configuration file dictates.");
                    last = selection;
                }
            } catch (Exception ignored) {
                // ignoring configuration preference, as admin didn't provide correct format
                logger.debug("Import date selection for Ceilometer ignored - use yyyy-MM-dd'T'HH:mm:ssZ format");
            }
        }
        DateTime dateTime = last.toDateTime(DateTimeZone.UTC);
        return dateTime;
    }
}