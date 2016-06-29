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

package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.data.mapping.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.OsloEvent;
import ch.icclab.cyclops.consume.data.mapping.OsloEvent.OsloMessage.Args;
import ch.icclab.cyclops.consume.data.mapping.OsloEvent.OsloMessage.Args.ObjInst.Nova_objectData;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;


/**
 * Author: Skoviera
 * Created: 14/04/16
 * Updated: Oleksii 01/06/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    final static Logger logger = LogManager.getLogger(DataConsumer.class.getName());
    private static InfluxDBClient influxDBClient = InfluxDBClient.getInstance();

    @Override
    protected void consume(String content) {
        try {
            OpenstackEvent data = manageMessage(content);
            if (data.isValid()){
                influxDBClient.persistSinglePoint(data.getPoint());
            }

        } catch (Exception ignored) {

        }
    }


    private OpenstackEvent manageMessage(String content) {
        Gson mapper = new Gson();
        OpenstackEvent openstackEvent = new OpenstackEvent();
        List<String> listOfActions  = Arrays.asList( "pausing",
                "unpausing", "[powering-off]",
                "powering-on", "suspending", "resuming",
                "deleted", "spawning", "resize_finish");
        try {
            String method = "";
            OsloEvent osloEvent = mapper.fromJson(content, OsloEvent.class);
            Args args = osloEvent.getOsloMessage().getArgs();
            try {
                method = args.getKwargs().getExpected_task_state().toString();
            } catch (Exception ignored){

            }

            if (args.getObjmethod().equals("destroy" ))  {
                if (args.getObjinst().getNova_objectName().equals("Instance")) {
                    method = "deleted";
                }
            }
            if(listOfActions.contains(method)) {
                Nova_objectData novaData = args.getObjinst().getNova_objectData();
                String instanceId = novaData.getUuid();
                logger.trace("Instance" + instanceId + "changed a status to:" + method);
                openstackEvent.setAction(method);
                openstackEvent.setInstanceId(instanceId);
                openstackEvent.setUserName(osloEvent.getOsloMessage().get_context_user_name());
                openstackEvent.setService_type("novaInstanceUpTime");
                openstackEvent.setMemory(novaData.getMemory_mb());
                openstackEvent.setVcpus(novaData.getVcpus());

                return openstackEvent;

            }
        } catch (Exception ignored) {
            // this means it was not an array to begin with, just a simple object

        }
        return openstackEvent;

    }



}
