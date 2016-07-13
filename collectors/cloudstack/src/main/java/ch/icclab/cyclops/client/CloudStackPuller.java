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
package ch.icclab.cyclops.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.CloudStackSettings;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.LatestPullORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 20-Oct-15
 * Description: Class that connects to CloudStack using Auth module and pulls data
 */
public class CloudStackPuller {
    final static Logger logger = LogManager.getLogger(CloudStackPuller.class.getName());

    // construct CloudStack API URL
    private static CloudStackAuth auth;
    private static Integer pageSize;
    private final CloudStackSettings settings;

    /**
     * Simple constructor that will create Auth object and ask for CloudStack's Page size setting
     */
    public CloudStackPuller() {
        auth = new CloudStackAuth();
        pageSize = auth.getPageSize();
        settings = Loader.getSettings().getCloudStackSettings();
    }

    /**
     * Class is being used as container for API command and its parameters
     */
    protected class APICall {
        private String command;
        private HashMap<String, String> parameters;

        /**
         * Basic constructor for APICall command and parameters
         *
         * @param command
         * @param parameters
         */
        public APICall(String command, HashMap<String, String> parameters) {
            this.command = command;
            this.parameters = parameters;
        }

        public String getCommand() {
            return command;
        }

        public HashMap<String, String> getParameters() {
            return parameters;
        }
    }

    /**
     * Class is being used for creating API Call requests (mainly command and parameters)
     */
    private class APICallBuilder {
        private String _command;
        private HashMap<String, String> _parameters;

        /**
         * Constructor that will create command and empty map for parameters
         *
         * @param command that is going to be used in API call
         */
        public APICallBuilder(String command) {
            this._command = command;
            this._parameters = new HashMap<String, String>();
        }

        /**
         * This method will construct API call based on provided command and parameters
         *
         * @return
         */
        public APICall buildAPICall() {
            return new APICall(this._command, this._parameters);
        }

        /**
         * This method will add another parameter as key, value map entry
         *
         * @param key   for has hmap
         * @param value for hash map
         * @return
         */
        public APICallBuilder addParameter(String key, String value) {
            // add new line to parameters
            this._parameters.put(key, value);

            // return builder
            return this;
        }

        /**
         * Add date intervals as 'startdate' and 'enddate'
         *
         * @param dates as DateInterval
         * @return object of APICallBuilder
         */
        public APICallBuilder addDates(DateInterval dates) {
            return addParameter("startdate", dates.getFromDate()).addParameter("enddate", dates.getToDate());
        }

        /**
         * Support for pagination, add page to API Call
         *
         * @param page integer
         * @return continuation on APICall builder
         */
        public APICallBuilder addPage(Integer page) {
            return addParameter("page", page.toString()).addParameter("pagesize", pageSize.toString());
        }

    }

    /**
     * This class is being used to generate interval either from last point or epoch
     */
    private class DateInterval {
        private String fromDate;
        private String toDate;

        protected DateInterval(DateTime from) {
            fromDate = from.toString("yyyy-MM-dd");
            toDate = new LocalDate().toString("yyyy-MM-dd");
        }

        protected String getFromDate() {
            return fromDate;
        }

        protected String getToDate() {
            return toDate;
        }
    }

    /**
     * Will determine when was the last entry point (pull from CloudStack), or even if there was any
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
        String date = settings.getCloudStackImportFrom();
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
                logger.debug("Import date selection for CloudStack ignored - use yyyy-MM-dd format");
            }
        }

        return last;
    }

    /**
     * This method constructs signed URL for desired page
     *
     * @param dates              DateInterval object with start and end dates
     * @param page               Integer stating what page has to be signed
     * @return string URL for particular page
     */
    private String signPage(DateInterval dates, Integer page) {
        APICall apiCall = getListUsageRecordsCall(dates, page);
        return auth.getSignedURL(apiCall);
    }

    /**
     * Pull, retrieve and parse UsageRecords from CloudStack
     * @return container with all retrieved points
     */
    private Boolean pull() {
        // whether to start from epoch or last commit
        DateInterval dates = new DateInterval(whenWasLastPull());

        // prepare for the first API call (we need number of usage records)
        String firstPage = signPage(dates, 1);
        CloudStackDownloader firstRun = new CloudStackDownloader(firstPage);

        // first run has to be manual (not threaded)
        List<Object> firstPageRecords = firstRun.performRequest();

        // only if we have valid list
        if (firstPageRecords != null) {

            // here is the point when everything is downloaded, so lets save first page and then the rest
            broadcastRecords(firstPageRecords);

            // now calculate if we need to load some more
            Integer remainingRecords = firstRun.getCount() - pageSize;
            logger.debug("Total records: " + firstRun.getCount().toString() + " remaining records: " + remainingRecords.toString());

            // if there is something to process
            if (remainingRecords > 0) {
                // compute number of remaining pages
                Integer lastPage = (remainingRecords % pageSize == 0) ? 0 : 1;
                Integer remainingPages = remainingRecords / pageSize + lastPage;
                logger.debug("Remaining pages to process: " + remainingPages.toString());

                // now collect the rest
                for (int i = 0; i < remainingPages; i++) {
                    CloudStackDownloader dl = new CloudStackDownloader(signPage(dates, i + 2));
                    List<Object> records = dl.performRequest();
                    if (records != null) {
                        broadcastRecords(records);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Broadcast list of items on RabbitMQ
     * @param list to be broadcast
     */
    private void broadcastRecords(List<Object> list) {
        Messenger.getInstance().broadcast(list);
    }

    /**
     * Get data from CloudStack and parse it into list of UsageData objects (with pagination support)
     *
     * @return whether operation was successful
     */
    public Boolean pullUsageRecords() {
        logger.trace("Trying to pull Usage Records from Vanilla CloudStack");

        Boolean status = pull();

        if (status) {
            logger.trace("Usage Records successfully pulled from Vanilla CloudStack");
        } else {
            logger.error("Couldn't pull Usage Records from Vanilla CloudStack, consult logs");
        }

        return status;
    }

    /**
     * Construct listUsageRecords API Call command
     *
     * @param dates object consisting from and to dates
     * @param page  number (depends on CloudStack's pagesize)
     * @return listUsageRecords command
     */
    private APICall getListUsageRecordsCall(DateInterval dates, Integer page) {
        return new APICallBuilder("listUsageRecords").addDates(dates).addPage(page).buildAPICall();
    }
}
