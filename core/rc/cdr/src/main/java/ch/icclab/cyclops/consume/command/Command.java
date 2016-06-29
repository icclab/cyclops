package ch.icclab.cyclops.consume.command;
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

/**
 * Author: Skoviera
 * Created: 07/03/16
 * Description: Pojo object for automatic GSON mapping of Commands
 */
public abstract class Command {
    private String _class;
    protected static String FIELD_FOR_MAPPING = "_class";

    /**
     * Every command has to implement execute method
     */
    protected abstract void execute();

    //===== Getters and Setters
    public String get_class() {
        return _class;
    }
    public void set_class(String _class) {
        this._class = _class;
    }
}
