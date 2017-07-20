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

import ch.icclab.cyclops.facts.PersistedFact;
import ch.icclab.cyclops.persistence.HibernateClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 01/03/16
 * Description: Own Rule Runtime event listener
 */
public class RuleRuntimeEventListener extends DefaultRuleRuntimeEventListener {
    final static Logger logger = LogManager.getLogger(RuleRuntimeEventListener.class.getName());

    HibernateClient client = HibernateClient.getInstance();

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object obj = event.getObject();
        String name = obj.getClass().getSimpleName();
        logger.trace(String.format("Object of class (%s) inserted", name));

        try {
            // persist if needed
            if (((PersistedFact) obj).isStateful()) {

                logger.trace(String.format("Persisting inserted object (%s) into database", name));

                client.persistObject(obj);
            }
        } catch (Exception ignored) {
            // TODO: shall we do something about it? it might have failed because database is down
            logger.error(String.format("%s", ignored.getMessage()));
        }

        super.objectInserted(event);
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        Object obj = event.getOldObject();
        String name = obj.getClass().getSimpleName();
        logger.trace(String.format("Object of class (%s) deleted", name));

        try {
            // persist if needed
            if (((PersistedFact) obj).isStateful()) {

                logger.trace(String.format("Removing deleted object (%s) from database", name));

                client.deleteObject(obj);
            }
        } catch (Exception ignored) {
            // TODO: shall we do something about it? it might have failed because database is down
            logger.error(String.format("%s", ignored.getMessage()));
        }

        super.objectDeleted(event);
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        Object updated = event.getObject();
        String name = updated.getClass().getSimpleName();
        logger.trace(String.format("Object of class (%s) updated", name));

        try {
            // persist if needed
            if (((PersistedFact) updated).isStateful()) {
                logger.trace(String.format("Updating state of object (%s) and persisting it in database", name));

                // now update it
                client.persistObject(updated);
            }
        } catch (Exception ignored) {
            // TODO: shall we do something about it? it might have failed because database is down
            logger.error(String.format("%s", ignored.getMessage()));
        }

        super.objectUpdated(event);
    }
}
