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

import java.math.BigDecimal;

/**
 * Author: Skoviera
 * Created: 06/09/16
 * Description: Various utils for TimeStamp object manipulation
 */
public class TimeStamp {

    /**
     * Parse long from unknown type
     * @param unknown object
     * @return Long or null
     */
    public static Long cast(Object unknown) {
        try {
            BigDecimal dec = (BigDecimal) unknown;
            return dec.longValue();
        } catch (Exception a) {
            try {
                return Long.parseLong((String) unknown);
            } catch (Exception b) {
                try {
                    return (Long) unknown;
                } catch (Exception c) {
                    try {
                        return Long.valueOf(unknown.toString());
                    } catch (Exception d) {
                        return null;
                    }
                }
            }
        }
    }
}