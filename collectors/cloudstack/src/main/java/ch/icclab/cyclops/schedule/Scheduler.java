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

package ch.icclab.cyclops.schedule;

import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Implementation of scheduler for scheduling custom tasks and runners
 */
public class Scheduler {
    final static Logger logger = LogManager.getLogger(Scheduler.class.getName());

    // this class has to be a singleton
    private static Scheduler singleton = new Scheduler();

    // executor service (we only need one thread)
    private ScheduledExecutorService executor;
    private ExecutorService forceExecutor;

    // list of scheduled runners
    private List<RunnerEntry> listOfRunners;

    /**
     * This class holds settings for scheduled threads
     */
    private class RunnerEntry{
        private AbstractRunner clazz;
        private Long delay;
        private Long frequency;
        private TimeUnit timeUnit;

        public AbstractRunner getClazz() {
            return clazz;
        }
        public void setClazz(AbstractRunner clazz) {
            this.clazz = clazz;
        }
        public Long getDelay() {
            return delay;
        }
        public void setDelay(Long delay) {
            this.delay = delay;
        }
        public Long getFrequency() {
            return frequency;
        }
        public void setFrequency(Long frequency) {
            this.frequency = frequency;
        }
        public TimeUnit getTimeUnit() {
            return timeUnit;
        }
        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }
    }

    /**
     * We need to hide constructor from public
     */
    private Scheduler() {
        this.executor = null;
        listOfRunners = new ArrayList<RunnerEntry>();
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static Scheduler getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     * NOTE: If you need to execute tasks in parallel, use newScheduledThreadPool(number) instead of newSingleThreadScheduledExecutor
     */
    public void start() {

        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();

            logger.trace("Starting up internal scheduler");

            // schedule all necessary threads
            for (RunnerEntry runner: listOfRunners) {
                executor.scheduleAtFixedRate(runner.getClazz(), runner.getDelay(), runner.getFrequency(), runner.getTimeUnit());
            }
        }
    }

    /**
     * Stops execution run
     */
    public void stop() {
        if (executor != null) {

            logger.trace("Stopping internal scheduler's execution run");

            executor.shutdownNow();
            executor = null;
        }

        // also stop forced tasks
        stopForce();
    }

    /**
     * Returns whether scheduler is running or not
     * @return status
     */
    public Boolean isRunning() {
        return (executor != null);
    }

    /**
     * Stop currently running tasks that were force started
     */
    private void stopForce() {
        if (forceExecutor != null) {

            logger.trace("Halting down internal scheduler's running tasks");

            forceExecutor.shutdownNow();
            forceExecutor = null;
        }
    }

    /**
     * Manually (on top of scheduler) force update
     * NOTE: If you need to execute tasks in parallel, use newFixedThreadPool(number) instead of newSingleThreadExecutor
     */
    public void force() {

        // first stop force tasks
        stopForce();

        // create new executor
        forceExecutor = Executors.newSingleThreadExecutor();

        logger.trace("Forcing internal scheduler to run planned tasks manually");

        // start them manually, but only once
        for (RunnerEntry runner: listOfRunners) {
            forceExecutor.execute(runner.getClazz());
        }
    }

    /**
     * Add another Runnable object to the list
     * @param clazz Object.class
     * @param delay in seconds
     * @param frequency in seconds
     */
    public void addRunner(AbstractRunner clazz, long delay, long frequency, TimeUnit unit) {
        RunnerEntry runner = new RunnerEntry();

        logger.trace("Adding new scheduled runner for later execution");

        runner.setClazz(clazz);
        runner.setDelay(delay);
        runner.setFrequency(frequency);
        runner.setTimeUnit(unit);

        listOfRunners.add(runner);
    }
}
