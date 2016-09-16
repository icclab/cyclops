package ch.icclab.cyclops.executor;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Skoviera
 * Created: 09/08/16
 * Description: Global task executor
 */
public class TaskExecutor {
    final static Logger logger = LogManager.getLogger(TaskExecutor.class.getName());
    private static TaskExecutor singleton = new TaskExecutor();

    private ExecutorService executor;

    private TaskExecutor(){
        executor = obtainSession();
    }

    private ExecutorService obtainSession() {
        // create a pool threads (based on available cpu cores)
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static TaskExecutor getInstance() { return singleton; }

    public void addTask(Runnable task) {
        if (executor == null) {
            executor = obtainSession();
        }
        executor.submit(task);
    }

    public void shutDown() {
        if (executor != null) {
            logger.trace("Shutting down Task Executor");
            executor.shutdownNow();
            executor = null;
        }
    }
}
