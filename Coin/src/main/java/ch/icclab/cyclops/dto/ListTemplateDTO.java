package ch.icclab.cyclops.dto;
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

import ch.icclab.cyclops.persistence.orm.TemplateORM;
import ch.icclab.cyclops.util.RegexParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 22/02/16
 * Description: POJO object for list of templates, so we have it nicely formatted
 */
public class ListTemplateDTO {
    private List<Entry> templates;

    private class Entry {
        private Long id;
        private String name;
        private String added;
        private List<String> fields;

        public void setId(Long id) {
            this.id = id;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setFields(List<String> fields) {
            this.fields = fields;
        }
        public void setAdded(String added) {
            this.added = added;
        }
    }

    /**
     * Populate internal list of templates
     * @param templateList list
     */
    public ListTemplateDTO(List<TemplateORM> templateList) {
        templates = new ArrayList<Entry>();

        // iterate over list of templates
        for (TemplateORM template : templateList) {
            Entry entry = new Entry();

            // fill items
            entry.setId(template.getId());
            entry.setName(template.getName());
            entry.setFields(RegexParser.getFieldsFromTemplate(template.getRuleTemplate()));
            entry.setAdded(template.getAdded());

            // add it to the list
            templates.add(entry);
        }
    }

    public List<Entry> getTemplates() {
        return templates;
    }
}
