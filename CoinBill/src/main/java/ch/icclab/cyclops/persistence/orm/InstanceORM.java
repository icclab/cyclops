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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;
import java.util.*;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 22/02/16
 * Description: Pojo object for mapping Rule instances
 * Note: Templates and their Instances are not automatically linked over Hibernate
 *       as we still don't know what behaviour we prefer when deleting them
 */
@Entity
public class InstanceORM{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String rule;

    private Long templateId;

    @Column(columnDefinition = "TEXT")
    private String name;

    private String added;

    @Column(columnDefinition = "TEXT")
    private String fieldNames;

    @Column(columnDefinition = "TEXT")
    private String fieldValues;

    public InstanceORM() {}

    public InstanceORM(Long id) {
        this.id = id;
    }

    /**
     * Create rule from provided template
     * @param template content
     * @param fieldsAndValues values to use for instantiation
     */
    public InstanceORM(TemplateORM template, Map fieldsAndValues) {
        this.templateId = template.getId();

        setRule(template.getRuleTemplate());

        this.fieldNames = getListAsString(fieldsAndValues.keySet());
        this.fieldValues = getListAsString(fieldsAndValues.values());
    }

    /**
     * Create rule from provided content
     * @param content of rule
     */
    public InstanceORM(String content) {
        setRule(content);
    }

    /**
     * Used for Template instantiation
     * @param rule definition
     */
    public void setRule(String rule) {
        this.name = RegexParser.getNameFromRule(rule);
        this.added = getCurrentTime();
        this.rule = applyTimeStampToRuleDefinition(rule, added);
    }

    /**
     * Get rule name that has timestamp applied
     * @return string
     */
    public String getStampedName() {
        return RegexParser.getNameFromRule(this.rule);
    }

    /**
     * Current time
     * @return string
     */
    private String getCurrentTime() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m:s.S d/MMM/y");
        return fmt.print(new DateTime());
    }

    /**
     * Apply time stamp to rule definition
     * @return string
     */
    private String applyTimeStampToRuleDefinition(String rule, String timeStamp) {
        String name = RegexParser.getNameFromRule(rule);
        String formatted = String.format("%s (%s)", name, timeStamp);

        return StringUtils.replaceOnce(rule, name, formatted);
    }

    /**
     * Parse List and return it as serialised string
     * @param list to be serialised
     * @return string
     */
    private String getListAsString(Set list) {
        return String.join(",", list);
    }
    private String getListAsString(Collection col) {
        List<String> list = new ArrayList<>();
        col.stream().forEach(elt -> list.add(elt.toString()));
        return String.join(",", list);
    }

    /**
     * Deserialize string and return list instead
     * @param content to be parsed
     * @return list of strings
     */
    private List<String> getStringAsList(String content) {
        List<String> list;
        try {
            list = new ArrayList<>(Arrays.asList(content.split(",")));
        } catch (Exception ignored) {
            // let's return empty list
            list = new ArrayList<>();
        }
        return list;
    }

    //===== Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getRule() {
        return rule;
    }

    public Long getTemplateId() {
        return templateId;
    }
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getFieldValues() {
        return fieldValues;
    }
    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }

    public String getFieldNames() {
        return fieldNames;
    }
    public void setFieldNames(String fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAdded() {
        return added;
    }
    public void setAdded(String added) {
        this.added = added;
    }

    public List<String> getFieldNamesAsList() {
        return getStringAsList(fieldNames);
    }

    public List<String> getFieldValuesAsList() {
        return getStringAsList(fieldValues);
    }

    public Map<String, String> getFieldsAsMap() {
        // first get keys and values
        List<String> keys = getFieldNamesAsList();
        List<String> values = getFieldValuesAsList();

        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Cannot combine lists with different sizes");
        } else {
            // combine them together
            Map<String,String> map = new LinkedHashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
            return map;
        }
    }
}
