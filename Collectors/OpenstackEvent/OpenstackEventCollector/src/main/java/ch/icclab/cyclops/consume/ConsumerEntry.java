package ch.icclab.cyclops.consume;
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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 21.02.17
 * Description: RabbitMQ process entry
 */
public class ConsumerEntry {
    private AbstractConsumer clazz;
    private String queue;
    private Consumer consumer;
    private Channel channel;
    private String tag;
    private int prefetch;

    public ConsumerEntry(AbstractConsumer cl, Connection con, String q, int p) throws Exception {
        clazz = cl;
        queue = q;
        prefetch = p;

        // create channel and declare queues
        channel = con.createChannel();
        channel.queueDeclare(queue, true, false, false, null);

        // create process
        consumer = clazz.handleDelivery(this);


        // set prefetch limit
        channel.basicQos(prefetch, false);

        // start consuming and save the tag
        tag = channel.basicConsume(queue, false, consumer);
    }

    /**
     * Close channel of the process
     */
    public void shutDown() {
        try {
            channel.basicCancel(tag);
        } catch (Exception ignored) {}

        try {
            channel.close();
        } catch (Exception ignored) {}
    }

    public Channel getChannel() {
        return channel;
    }

    public void ackMessage(long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception ignored) {
        }
    }

    public void nackMessage(long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (Exception ignored) {
        }
    }
}
