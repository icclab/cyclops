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
package ch.icclab.cyclops.load.model;

/**
 * Author: Skoviera
 * Created: 25/01/16
 * Description: Rating preferences
 */
public class RatingPreferences {

    public static String DEFAULT_USAGE_FIELD = "usage";
    public static String DEFAULT_CHARGE_FIELD = "charge";

    public static String DEFAULT_CHARGE_SUFFIX = "CDR";
    public static String NEW_UDR_CLASS_NAME = "UDR";

    public static Double DEFAULT_RATE_VALUE = 1.0d;
    public static String CLASS_FIELD_NAME = "_class";

    // These fields correspond with the configuration file
    private String usageField;
    private String chargeField;
    private Double defaultRate;
    private String chargeSuffix;

    //==== Getters and Setters

    public String getUsageField() {
        return usageField;
    }

    public void setUsageField(String usageField) {
        this.usageField = usageField;
    }

    public String getChargeField() {
        return chargeField;
    }

    public void setChargeField(String chargeField) {
        this.chargeField = chargeField;
    }

    public Double getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(Double defaultRate) {
        this.defaultRate = defaultRate;
    }

    public String getChargeSuffix() {
        return chargeSuffix;
    }

    public void setChargeSuffix(String chargeSuffix) {
        this.chargeSuffix = chargeSuffix;
    }
}
