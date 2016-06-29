package ch.icclab.cyclops.consume.command.model;
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

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.publish.Messenger;

/**
 * Author: Skoviera
 * Created: 29/04/16
 * Description: Command representing External bill request
 */
public class GenericBillRequest extends Command {

    private String subject;

    @Override
    protected void execute() {
        // send this request to Rule Engine via queue
        Messenger.getInstance().broadcast(this);
    }

    //====== Getters and Setters
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
