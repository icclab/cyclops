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
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/03/16
 * Description: Properly log rule execution
 */
public class AuditRuleExecution extends DefaultAgendaEventListener {
    final static Logger logger = LogManager.getLogger(AuditRuleExecution.class.getName());

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        logger.log(LogConstants.RULE, String.format("Rule match fired for: \"%s\", on following objects: %s", event.getMatch().getRule().getName(), new Gson().toJson(event.getMatch().getObjects())));
        super.afterMatchFired(event);
    }
}
