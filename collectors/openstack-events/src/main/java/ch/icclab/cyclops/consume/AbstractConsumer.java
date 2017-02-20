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

package ch.icclab.cyclops.consume;

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.OpenstackSettings;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.InfluxDBHealth;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 25/01/16
 * Description: Abstract consumer for our RabbitMQ
 */
public abstract class AbstractConsumer {

    protected static InfluxDBClient influxDBClient = new InfluxDBClient();
    protected static OpenstackSettings settings = Loader.getSettings().getOpenstackSettings();
    protected static InfluxDBHealth influxDBHealth = InfluxDBHealth.getInstance();

    /**
     * This is a method to transform message into OpenstackEvent object
     * @param content message itself
     * @return OpenstackEvent object
     */
    protected abstract OpenstackEvent manageMessage(String content);

    protected boolean consume(String content) {
        OpenstackEvent data;
        try{
            data = manageMessage(content);
        }catch (Exception ignored) {
            return true;
        }
        try{
            if (data != null) {
                influxDBClient.persistSinglePoint(data.getPoint());
            }
            return true;
        } catch (Exception ignored){
            return  false;
        }
    }


    /**
     * This is the body of message processing
     * @param channel where consumer should listen
     * @return consumer object
     */
    public Consumer handleDelivery(Channel channel) {
        return new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // make sure encoding is correct
                String message = new String(body, "UTF-8");
                // and let the message be consumed by client
                boolean status = consume(message);
                if (status){
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    influxDBHealth.setStatusOfLastSave(true);
                } else {
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                    influxDBHealth.setStatusOfLastSave(false);
                    int time = new Integer(settings.getOpenstackScheduleTime());
                    try{
                        TimeUnit.MILLISECONDS.sleep(time);
                    } catch (Exception ignored){

                    }

                }

            }

        };
    }

    /**
     * This is method to simplify openstatck event actions
     * @param method a method fetched from message
     * @return collector friendly method
     */
    protected String getType(String method){
        List<String> listOfActiveActions = Arrays.asList("spawning", "powering-on", "unpausing", "resuming",
                "floatingip.create.end", "volume.create.end", "resize_finish", "volume.resize.end");
        String paused = "pausing";
        String stopped="[powering-off]";
        String suspended = "suspending";
        List<String> listOfDeletedActions = Arrays.asList ("floatingip.delete.end", "volume.delete.end");

        if (listOfActiveActions.contains(method)){
            return settings.getOpenstackCollectorEventRun();
        }
        if (method.equals(paused)){
            return settings.getOpenstackCollectorEventPause();
        }
        if (method.equals(stopped)){
            return settings.getOpenstackCollectorEventStop();
        }
        if (method.equals(suspended)){
            return settings.getOpenstackCollectorEventSuspend();
        }
        if (listOfDeletedActions.contains(method)){
            return settings.getOpenstackCollectorEventDelete();
        }
        return method;
    }
}
