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

import com.google.gson.annotations.Expose;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 15-Oct-15
 * Description: POJO object for Generic Usage Data
 */
public abstract class UsageData {

    private static String PREFIX = "CloudStack";

    @Expose
    private String _class;

    // Name of the account
    @Expose
    private String account;

    // Measurement's timestamp
    @Expose
    private Long time;

    // Measurement's usage
    @Expose
    private Object usage;

    // Measurement's unit
    @Expose
    private Object unit = "hours";

    // Metadata container
    @Expose
    private Map metadata;

    //////////////////////////////////////////////////////
    //==== everything below this goes into metadata ====//
    //////////////////////////////////////////////////////

    // ID of the account
    private String accountid;

    // ID of the domain in which this account resides
    private String domainid;

    // A string describing what the usage record is tracking
    private String description;

    // The range of time for which the usage is aggregated
    private String startdate;
    private String enddate;

    // Virtual machine
    private String usageid;

    // A number representing the usage type (see Usage Types)
    private Integer usagetype;

    // A number representing the actual usage in hours
    private String rawusage;

    // Zone where the usage occurred
    private String zoneid;

    // In case we don't have user but a project
    private String project;
    private String projectid;

    // The ID of the disk offering
    private String offeringid;

    public UsageData() {
        setClassName();
    }

    /**
     * Make sure we are not having any null values
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    public String get_class() {
        return _class;
    }
    public void set_class(String _class) {
        this._class = _class;
    }
    protected void setClassName() {
        this.set_class(String.format("%s%s",UsageData.PREFIX, this.getClass().getSimpleName()));
    }

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

    public void prepareForSending() {
        usage = Double.parseDouble(rawusage);
        time = getMilisForTime(startdate);

        // add global metadata
        addToMetadata("accountId", accountid);
        addToMetadata("domainId", domainid);
        addToMetadata("source", usageid);
        addToMetadata("description", description);
        addToMetadata("zoneId", zoneid);
        addToMetadata("project", project);
        addToMetadata("projectId", projectid);
        addToMetadata("offeringId", offeringid);

        // ask children to add their metadata
        additionalMetadata(metadata);
    }

    protected abstract void additionalMetadata(Map map);

    protected void addToMetadata(String str, Object obj) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        // don't add null objects
        if (str != null && !str.isEmpty() && obj != null) {
            metadata.put(str, obj);
        }
    }

    /////////////////////////////
    // Getters and Setters
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(String domainid) {
        this.domainid = domainid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public Object getUsage() {
        return usage;
    }
    public void setUsage(Object usage) {
        this.usage = usage;
    }

    public String getUsageid() {
        return usageid;
    }

    public void setUsageid(String usageid) {
        this.usageid = usageid;
    }

    public Integer getUsagetype() {
        return usagetype;
    }

    public void setUsagetype(Integer usagetype) {
        this.usagetype = usagetype;
    }

    public String getRawusage() {
        return rawusage;
    }

    public void setRawusage(String rawusage) {
        this.rawusage = rawusage;
    }

    public String getZoneid() {
        return zoneid;
    }

    public void setZoneid(String zoneid) {
        this.zoneid = zoneid;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectid() {
        return projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getOfferingid() {
        return offeringid;
    }

    public void setOfferingid(String offeringid) {
        this.offeringid = offeringid;
    }

    public Map getMetadata() {
        return metadata;
    }

    public Object getUnit() {
        return unit;
    }

    public void setUnit(Object unit) {
        this.unit = unit;
    }
}
