package ch.icclab.cyclops.health;
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

import ch.icclab.cyclops.application.Main;
import ch.icclab.cyclops.consume.ConsumeManager;
import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.ServerSettings;
import ch.icclab.cyclops.publish.RabbitMQPublisher;
import com.google.common.base.Stopwatch;
import org.joda.time.Period;

import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 10.02.17
 * Description: Global health status of the micro service
 */
public class HealthStatus {
    private static HealthStatus singleton = new HealthStatus();

    // is the micro service in a healthy status?
    private boolean healthy;

    // reason for being unhealthy
    private String reason;

    // how to reschedule subsequent Health checks
    private long period = Loader.getSettings().getServerSettings().getServerHealthCheck();
    private boolean kill = Loader.getSettings().getServerSettings().getServerHealthShutdown();
    private TimeUnit unit = ServerSettings.SERVER_HEALTH_CHECK_UNIT;

    // stopwatch to see the status
    private Stopwatch watch;

    public synchronized static HealthStatus getInstance() { return singleton; }
    private HealthStatus() {
        this.healthy = true;
        this.reason = null;
        this.watch = Stopwatch.createStarted();
    }

    public class Health {
        private Period period;
        private boolean alive;

        public Health(boolean alive, Period period) {
            this.period = period;
            this.alive = alive;
        }

        public Period getPeriod() {
            return period;
        }

        public boolean isAlive() {
            return alive;
        }
    }

    /**
     * Health status of the micro service
     * @return Health status
     */
    public synchronized Health getHealth() {
        Period period = new Period(watch.elapsed(TimeUnit.MILLISECONDS));
        return new Health(healthy, period);
    }

    /**
     * Reason for an unhealthy micro service
     * @return String reason or null
     */
    public synchronized String getReason() {
        return reason;
    }

    /**
     * Restore the microservice
     */
    protected synchronized void setHealthy() {
        // if not already healthy
        if (!healthy) {
            healthy = true;
            reason = null;
            resetWatch();

            // stop remaining scheduled checks
            TaskExecutor.getInstance().shutDown();

            // try to start publisher
            if (RabbitMQPublisher.getInstance() == null) {
                healthy = false;
            }

            // try to start consumer
            if (!ConsumeManager.startConsumption()) {

                healthy = false;

                // shut down publisher
                RabbitMQPublisher.shutDown();
            }

            // schedule micro service check
            scheduleCheck();
        }
    }

    /**
     * Pause the microservice and schedule a check
     */
    protected synchronized void setUnhealthy(String why) {
        if (healthy) {
            healthy = false;
            reason = why;
            resetWatch();

            // cancel all tasks in the task executor service
            TaskExecutor.getInstance().shutDown();

            // stop listening for commands and pause data ingestion
            ConsumeManager.stopConsumption();

            // shut down also RabbitMQ Publisher
            RabbitMQPublisher.shutDown();

            // optionally kill the micro service
            if (kill) System.exit(Main.ERR_HEALTH);

            // schedule micro service check
            scheduleCheck();
        }
    }

    /**
     * Schedule another micro service check
     */
    public synchronized void scheduleCheck() {
        TaskExecutor.getInstance().addScheduledTask(new HealthCheck(), period, period, unit);
    }

    /**
     * Reset elapsed time
     */
    private void resetWatch() {
        if (watch == null) {
            watch = Stopwatch.createStarted();
        }

        watch.reset();
        watch.start();
    }
}
