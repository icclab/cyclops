package ch.icclab.cyclops.schedule.runner.openstack;

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

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackCinderEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNovaEvent;
import ch.icclab.cyclops.consume.data.mapping.usage.OpenStackVolumeActiveUsage;
import ch.icclab.cyclops.persistence.HibernateClient;

import ch.icclab.cyclops.persistence.pulls.LatestPullCinder;
import ch.icclab.cyclops.schedule.runner.OpenStackClient;
import ch.icclab.cyclops.util.loggers.SchedulerLogger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Map;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Cinder usage records out of events and send to the queue
 */
public class CinderUDRRunner extends OpenStackClient {

    @Override
    public String getDbName() {
        return OpenstackCinderEvent.class.getSimpleName();
    }

    @Override
    public Class getUsageFormat(){
        return OpenstackCinderEvent.class;
    }

    @Override
    public ArrayList<OpenStackVolumeActiveUsage> generateValue(Long eventTime, OpenstackEvent lastEventInScope) {
        OpenstackCinderEvent transformedEvent = (OpenstackCinderEvent) lastEventInScope;
        Long eventLastTime = transformedEvent.getTime();
        ArrayList<OpenStackVolumeActiveUsage> generatedUsages = new ArrayList<>();
        generatedUsages.add(new OpenStackVolumeActiveUsage(eventLastTime, transformedEvent.getAccount(),
                transformedEvent.getVolumeName(), transformedEvent.getSource(),
                (double) (eventTime - eventLastTime) / 1000, transformedEvent.getDisk())); //Seconds instead of milliseconds;
        return generatedUsages;
    }

    @Override
    public void updateLatestPull(Long time) {
        LatestPullCinder pull = (LatestPullCinder) hibernateClient.getObject(LatestPullCinder.class, 1l);
        if (pull == null) {
            pull = new LatestPullCinder(time);
        } else {
            pull.setTimeStamp(time);
        }
        SchedulerLogger.log("The last pull set to " + pull.getTimeStamp().toString());
        hibernateClient.persistObject(pull);
    }

    @Override
    public DateTime getLatestPull() {
        DateTime last;
        LatestPullCinder pull = (LatestPullCinder) HibernateClient.getInstance().getObject(LatestPullCinder.class, 1l);
        if (pull == null) {
            last = new DateTime(0);
        } else {
            last = new DateTime(pull.getTimeStamp());
        }
        return last;
    }
}