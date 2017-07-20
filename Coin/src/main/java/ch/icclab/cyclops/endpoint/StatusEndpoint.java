package ch.icclab.cyclops.endpoint;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import ch.icclab.cyclops.health.HealthStatus;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.Map;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 21/01/16
 * Description: Serve application's version over root endpoint
 */
public class StatusEndpoint extends ServerResource {

    private PeriodFormatter formatter = new PeriodFormatterBuilder().appendYears().appendSuffix(" years ").appendMonths().appendSuffix(" months ")
            .appendWeeks().appendSuffix(" weeks ").appendDays().appendSuffix(" days ").appendHours().appendSuffix(" hours ").appendMinutes().appendSuffix(" minutes ")
            .appendSeconds().appendSuffix(" seconds ").printZeroNever().toFormatter();

    private final static String PARAM_TIME = "time";
    private final static String PARAM_REASON = "reason";

    @Get
    public String status(){
        // get health of the micro service
        HealthStatus health = HealthStatus.getInstance();
        HealthStatus.Health status = health.getHealth();
        String message = (status.isAlive())? "Healthy": "Unhealthy";

        // verbose or not
        Map<String, String> params = getQuery().getValuesMap();
        if (params != null) {

            // check for time stamp
            if (params.containsKey(PARAM_TIME) && Boolean.parseBoolean(params.get(PARAM_TIME))) {
                message = message.concat(String.format(" for the last %s", formatter.print(status.getPeriod())));
            }

            // check for reason
            if (params.containsKey(PARAM_REASON) && Boolean.parseBoolean(params.get(PARAM_REASON))) {
                String reason = health.getReason();
                if (reason != null && !reason.isEmpty()) {
                    message = message.concat(String.format(": %s", reason));
                }
            }
        }

        return message;
    }
}
