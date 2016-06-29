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

package ch.icclab.cyclops.load.model;

/**
 * Author: Oleksii
 * Created: 01/06/16
 * Description: Openstack Settings
 */

public class OpenstackSettings {
    private String openstackFirstImport;
    private String OpenstackCollectorEventStart;

    public void setOpenstackCollectorEventStart(String openstackCollectorEventStart) {
        OpenstackCollectorEventStart = openstackCollectorEventStart;
    }

    public void setOpenstackCollectorEventSpawn(String openstackCollectorEventSpawn) {
        OpenstackCollectorEventSpawn = openstackCollectorEventSpawn;
    }

    public void setOpenstackCollectorEventUnpause(String openstackCollectorEventUnpause) {
        OpenstackCollectorEventUnpause = openstackCollectorEventUnpause;
    }

    public void setOpenstackCollectorEventResume(String openstackCollectorEventResume) {
        OpenstackCollectorEventResume = openstackCollectorEventResume;
    }

    private String OpenstackCollectorEventSpawn;
    private String OpenstackCollectorEventUnpause;
    private String OpenstackCollectorEventResume;
    private String OpenstackEventTable;

    public String getOpenstackScheduleTime() {
        return OpenstackScheduleTime;
    }

    public void setOpenstackScheduleTime(String openstackScheduleTime) {
        OpenstackScheduleTime = openstackScheduleTime;
    }

    private String OpenstackScheduleTime;

    public String getOpenstackEventTable() {
        return OpenstackEventTable;
    }

    public void setOpenstackEventTable(String openstackEventTable) {
        OpenstackEventTable = openstackEventTable;
    }

    public String getOpenstackFirstImport() {
        return openstackFirstImport;
    }
    public String getOpenstackCollectorEventStart() {
        return OpenstackCollectorEventStart;
    }
    public String getOpenstackCollectorEventSpawn() {
        return OpenstackCollectorEventSpawn;
    }
    public String getOpenstackCollectorEventUnpause() {
        return OpenstackCollectorEventUnpause;
    }
    public String getOpenstackCollectorEventResume() {
        return OpenstackCollectorEventResume;
    }
    public void setOpenstackFirstImport(String openstackFirstImport) {
        this.openstackFirstImport = openstackFirstImport;

    }
}
