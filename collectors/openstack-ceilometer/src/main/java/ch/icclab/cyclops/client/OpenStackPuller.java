package ch.icclab.cyclops.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.OpenStackMeter;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 21/06/16.
 */

public class OpenStackPuller {
    final static Logger logger = LogManager.getLogger(OpenStackPuller.class.getName());

    private int pageSize = Loader.getSettings().getServerSettings().getPageSize();

    /**
     * Get data from OpenStack and parse it into list of UsageData objects (with pagination support)
     *
     * @return whether operation was successful
     */
    public Boolean pullUsageRecords() {
        logger.trace("Trying to pull Custom Usage Records from Vanilla OpenStack");

        Boolean status = pull();

        if (status) {
            logger.trace("Usage Records successfully pulled from Vanilla OpenStack");
        } else {
            logger.error("Couldn't pull Usage Records from Vanilla OpenStack, consult logs");
        }

        return status;
    }

    /**
     * Pull, retrieve and parse UsageRecords from OpenStack
     *
     * @return container with all retrieved points
     */
    private Boolean pull() {
        // whether to start from epoch or last commit
        DateInterval dates = new DateInterval(whenWasLastPull());
        String url = "";
        OpenStackMeterDownloader openStackMeterDownloader = new OpenStackMeterDownloader();
        ArrayList<OpenStackMeter> meters = openStackMeterDownloader.performRequest();

        List<Object> records = null;
        // build the url for requesting usage data from each meter
        for (OpenStackMeter meter : meters) {
            url = generateUsageUrl(dates, meter);
            OpenStackUsageDownloader openStackUsageDownloader = new OpenStackUsageDownloader(url);

            // first run has to be manual (not threaded)
            if (records == null) {
                records = openStackUsageDownloader.performRequest(meter);
            } else {
                List<Object> newRecords = openStackUsageDownloader.performRequest(meter);
                if (newRecords != null)
                    records.addAll(newRecords);
            }
        }
        // only if we have valid list
        if (records != null) {
            // here is the point when everything is downloaded, so lets save first page and then the rest
            broadcastRecords(records);

            return true;
        } else {
            return false;
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

    /**
     * Broadcast list of items on RabbitMQ
     *
     * @param list to be broadcast
     */
    private void broadcastRecords(List<Object> list) {
        for (Object item : list) {
            Messenger.getInstance().broadcast(item);
        }
    }

    /**
     * This class is being used to generate interval either from last point or epoch
     */
    private class DateInterval {
        private String fromDate;
        private String toDate;

        protected DateInterval(DateTime from) {
            fromDate = from.toString("yyyy-MM-dd'T'HH:mm:ssZ");
            toDate = new DateTime(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZ");
            if(fromDate.contains("+"))
                fromDate = fromDate.substring(0, fromDate.indexOf("+"));
            if(toDate.contains("+"))
                toDate = toDate.substring(0, toDate.indexOf("+"));
        }

        protected String getFromDate() {
            return fromDate;
        }

        protected String getToDate() {
            return toDate;
        }
    }

    private String generateUsageUrl(DateInterval dates, OpenStackMeter meter) {
        String metername = meter.getName();
        String type = meter.getType();
        String url = Loader.getSettings().getOpenStackSettings().getCeilometerUrl();

        String from = dates.getFromDate();
        String to = dates.getToDate();
        if (from != "" && to != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=gt&q.value=" + from + "&q.field=timestamp&q.op=lt&q.value=" + to + "&groupby=user_id&groupby=project_id&groupby=resource_id";
        else if (from != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=gt&q.value=" + from + "&groupby=user_id&groupby=project_id&groupby=resource_id";
        else if (to != "")
            url = url + "meters/" + metername + "/statistics?q.field=timestamp&q.op=lt&q.value=" + to + "&groupby=user_id&groupby=project_id&groupby=resource_id";

        return url;
    }

}
