package ch.icclab.cyclops.load.model;
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

import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 26/04/16
 * Description: Server settings
 */
public class ServerSettings {
    public static Integer DEFAULT_SERVER_HEALTH_CHECK = 30;
    public static TimeUnit SERVER_HEALTH_CHECK_UNIT = TimeUnit.SECONDS;

    private Integer serverHTTPPort;
    private Integer serverHealthCheck;
    private Boolean serverHealthShutdown;

    public Integer getServerHTTPPort() {
        return serverHTTPPort;
    }
    public void setServerHTTPPort(Integer serverHTTPPort) {
        this.serverHTTPPort = serverHTTPPort;
    }

    public Integer getServerHealthCheck() {
        return serverHealthCheck;
    }
    public void setServerHealthCheck(Integer serverHealthCheck) {
        this.serverHealthCheck = serverHealthCheck;
    }

    public Boolean getServerHealthShutdown() {
        return serverHealthShutdown;
    }
    public void setServerHealthShutdown(Boolean serverHealthShutdown) {
        this.serverHealthShutdown = serverHealthShutdown;
    }
}
