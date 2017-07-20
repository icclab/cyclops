package ch.icclab.cyclops.rule.listeners;
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

import ch.icclab.cyclops.util.LogConstants;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/03/16
 * Description: Properly log insertion, updates and subtraction of facts into working memory
 */
public class AuditTruthManagement extends DefaultRuleRuntimeEventListener {
    final static Logger logger = LogManager.getLogger(AuditTruthManagement.class.getName());

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object obj = event.getObject();
        logger.log(LogConstants.FACT, String.format("Object of class (%s) inserted, with the following content: %s", obj.getClass().getName(), new Gson().toJson(obj)));
        super.objectInserted(event);
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        Object obj = event.getOldObject();
        logger.log(LogConstants.FACT, String.format("Object of class (%s) deleted, with the following content: %s", obj.getClass().getName(), new Gson().toJson(obj)));
        super.objectDeleted(event);
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        Object obj = event.getObject();
        logger.log(LogConstants.FACT, String.format("Object of class (%s) updated, with the following content: %s", obj.getClass().getName(), new Gson().toJson(obj)));
        super.objectUpdated(event);
    }
}
