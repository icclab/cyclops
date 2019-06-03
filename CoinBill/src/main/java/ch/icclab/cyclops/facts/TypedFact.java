package ch.icclab.cyclops.facts;
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

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 07/03/16
 * Description: Fact type that represents JAVA POJO encoded facts, mapped based on "type" field available in JSON
 */
public class TypedFact extends MappedFact {
    public static final String TYPE_CONSTANT = "type";
    private String type;

    public TypedFact() {
    }

    //===== Getters and Setters
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
