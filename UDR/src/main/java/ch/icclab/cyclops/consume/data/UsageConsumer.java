package ch.icclab.cyclops.consume.data;
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

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.ConsumerEntry;
import ch.icclab.cyclops.executor.TaskExecutor;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Event consumer
 */
public class UsageConsumer extends AbstractConsumer {
    private TaskExecutor executor;

    public UsageConsumer() {
        this.executor = TaskExecutor.getInstance();
    }

    @Override
    public void consume(String content, ConsumerEntry consumer, Long deliveryTag) {
        executor.addTask(new UsageProcess(content, consumer, deliveryTag, true));
    }
}
