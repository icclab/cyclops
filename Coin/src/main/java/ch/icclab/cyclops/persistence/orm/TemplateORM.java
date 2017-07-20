package ch.icclab.cyclops.persistence.orm;
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

import ch.icclab.cyclops.util.RegexParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/02/16
 * Description: Pojo object for ORM mapping of Rule Templates
 */
@Entity
public class TemplateORM {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String ruleTemplate;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String listOfFields;

    private String added;

    // empty constructor
    public TemplateORM() {}

    public TemplateORM(Long id) {
        this.id = id;
    }

    // pre-filled constructor
    public TemplateORM(String template, String name) {
        this.ruleTemplate = template;
        this.name = name;
        this.added = getCurrentTime();
        List<String> list = RegexParser.getFieldsFromTemplate(template);
        this.listOfFields = String.join(",", list);
    }

    /**
     * Current time
     * @return string
     */
    private String getCurrentTime() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m:s.S d/MMM/y");
        return fmt.print(new DateTime());
    }

    //===== Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleTemplate() {
        return ruleTemplate;
    }
    public void setRuleTemplate(String ruleTemplate) {
        this.ruleTemplate = ruleTemplate;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getListOfFields() {
        return listOfFields;
    }
    public void setListOfFields(String listOfFields) {
        this.listOfFields = listOfFields;
    }

    public List<String> getFieldsAsList() {
        return new ArrayList<String>(Arrays.asList(listOfFields.split(",")));
    }

    public String getAdded() {
        return added;
    }
    public void setAdded(String added) {
        this.added = added;
    }
}