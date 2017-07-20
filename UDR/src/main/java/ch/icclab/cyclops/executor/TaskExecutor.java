package ch.icclab.cyclops.executor;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/08/16
 * Description: Global task executor
 */
public class TaskExecutor {
    final static Logger logger = LogManager.getLogger(TaskExecutor.class.getName());
    private static TaskExecutor singleton = new TaskExecutor();

    private ScheduledExecutorService executor;

    private TaskExecutor(){
        executor = obtainSession();
    }
    public static TaskExecutor getInstance() { return singleton; }

    /**
     * Create a pool threads (based on available cpu cores)
     * @return executor service
     */
    private ScheduledExecutorService obtainSession() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Add Runnable task for execution
     * @param task to add
     */
    public void addTask(Runnable task) {
        if (executor == null) executor = obtainSession();

        executor.submit(task);
    }

    /**
     * Add Runnable task for execution
     * @param task to add
     * @param delay to apply
     * @param period to use
     * @param unit to use
     */
    public void addScheduledTask(Runnable task, long delay, long period, TimeUnit unit) {
        if (executor == null) executor = obtainSession();

        executor.scheduleAtFixedRate(task, delay, period, unit);
    }

    /**
     * Execute runnable task now
     * @param task for execution
     */
    public void executeNow(Runnable task) {
        if (executor == null) executor = obtainSession();

        executor.execute(task);
    }

    /**
     * Terminate (with best effort) all threads
     */
    public void shutDown() {
        if (executor != null) {
            logger.trace("Shutting down Task Executor mercifully");
            executor.shutdown();
            executor = null;
        }
    }

    /**
     * Terminate (forcefully) all threads
     */
    public void forceShutDown() {
        if (executor != null) {
            logger.trace("Shutting down Task Executor forcefully");
            executor.shutdownNow();
            executor = null;
        }
    }
}
