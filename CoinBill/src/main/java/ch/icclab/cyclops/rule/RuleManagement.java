package ch.icclab.cyclops.rule;
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

import ch.icclab.cyclops.facts.MappedFact;
import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.PersistedFacts;
import ch.icclab.cyclops.persistence.HibernateClient;
import ch.icclab.cyclops.persistence.QueryHelper;
import ch.icclab.cyclops.persistence.orm.InstanceORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.rule.listeners.AuditRuleExecution;
import ch.icclab.cyclops.rule.listeners.AuditTruthManagement;
import ch.icclab.cyclops.rule.listeners.RuleRuntimeEventListener;
import ch.icclab.cyclops.rule.listeners.TrackingAgendaEventListener;
import ch.icclab.cyclops.util.loggers.FactsLogger;
import ch.icclab.cyclops.util.loggers.RulesLogger;
import ch.icclab.cyclops.util.loggers.TimelineLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 01/03/16
 * Description: Rule management and truth maintenance
 */
public class RuleManagement {
    final static Logger logger = LogManager.getLogger(RuleManagement.class.getName());

    // singleton
    private static RuleManagement singleton = new RuleManagement();

    // mandatory variables
    private KnowledgeBase base = null;
    private StatefulKnowledgeSession session = null;

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static RuleManagement getInstance() {
        return singleton;
    }

    /**
     * Default constructor
     */
    private RuleManagement() {
        base = KnowledgeBaseFactory.newKnowledgeBase();
    }

    /**
     * Wrapper for adding rule to production memory
     * @param instance to be added
     */
    public void addRule(InstanceORM instance) throws RuleException {
        if (instance != null) {
            if (instance.getTemplateId() != null) {
                // instantiate template
                String rule = instantiateTemplate(instance);

                // we want to persist old name, now timestamped one
                String oldRuleName = instance.getName();

                // save compiled rule
                instance.setRule(rule);
                instance.setName(oldRuleName);
            }

            // add that rule to the memory
            addRuleDirectly(instance);
        }
    }

    /**
     * Add rule by instantiating a template
     * @param rule containing template
     * @return instantiated string
     */
    private String instantiateTemplate(InstanceORM rule) {
        // create Drools template compiler
        ObjectDataCompiler compiler = new ObjectDataCompiler();

        // get parameters
        Collection<Map<String, String>> paramsMap = new ArrayList<>();
        Map<String, String> fields = rule.getFieldsAsMap();

        // populate fields from string
        Collection collection = Collections.singletonList(fields);
        InputStream template = new ByteArrayInputStream(rule.getRule().getBytes());

        // return compiled rule
        return compiler.compile(collection, template);
    }

    /**
     * Add rule directly without any template instantiations
     * @param rule to be added
     * @throws RuleException
     */
    private void addRuleDirectly(InstanceORM rule) throws RuleException {
        // put rule into envelope
        List<InstanceORM> oneLiner = new ArrayList<>();
        oneLiner.add(rule);

        // now load that rule
        loadRules(oneLiner);
    }

    /**
     * Remove rule from Production memory
     */
    public Boolean removeRule(InstanceORM rule){
        // iterate over all knowledge packages until rule is deleted
        for (KnowledgePackage kpcg: base.getKnowledgePackages()) {
            try {
                base.removeRule(kpcg.getName(), rule.getStampedName());

                return true;
            } catch (Exception ignored) {
                // we didn't find it in this package, moving on
            }
        }

        return false;
    }

    /**
     * Use provided rule instances to populate inference engine's production memory
     * @param rules to be loaded
     */
    public void loadRules(List<InstanceORM> rules) throws RuleException {
        if (rules != null && !rules.isEmpty()) {
            // new knowledge builder
            KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();

            // iterate over rules and add them to builder
            for (InstanceORM instance: rules) {
                builder.add(ResourceFactory.newByteArrayResource(instance.getRule().getBytes()), ResourceType.DRL);
            }

            // check for errors
            if (builder.hasErrors()) {
                String message = String.format("Couldn't add a new rule: %s", builder.getErrors().toString());
                logger.error(message);
                throw new RuleException(message);
            }

            // add loaded rules into knowledge base
            base.addKnowledgePackages(builder.getKnowledgePackages());
        }
    }

    /**
     * Ask for stateful knowledge session
     * @return session
     */
    private StatefulKnowledgeSession getNewStatefulKnowledgeSession () {
        // create stateful session
        StatefulKnowledgeSession session = base.newStatefulKnowledgeSession();

        // add listeners for auditing
        enableAuditingAndLogging(session);

        return session;
    }

    /**
     * Enable Auditing and Logging
     * @param session being enabled
     */
    private void enableAuditingAndLogging(StatefulKnowledgeSession session) {
        session.addEventListener(new AuditTruthManagement());
        session.addEventListener(new AuditRuleExecution());
    }

    /**
     * Enable Rule execution and Fact manipulation listeners
     * @param session being enabled
     */
    private void enableObjectListeners(StatefulKnowledgeSession session) {
        session.addEventListener(new TrackingAgendaEventListener());
        session.addEventListener(new RuleRuntimeEventListener());
    }

    /**
     * Call when we are done with initialisation so we can add object listeners
     * Otherwise preloaded facts would get saved repeatedly
     */
    private void doneWithInitialisation() {
        StatefulKnowledgeSession session = getSharedStatefulKnowledgeSession();
        enableObjectListeners(session);
    }

    /**
     * Ask for shared stateful knowledge session
     * @return shared session
     */
    private StatefulKnowledgeSession getSharedStatefulKnowledgeSession() {
        if (session == null) {
            session = getNewStatefulKnowledgeSession();
        }

        return session;
    }

    /**
     * Run rule just once and don't persist it
     * @param rule to be executed
     * @return result
     */
    public List<Object> fireJustOnce(InstanceORM rule) throws Exception {
        // add rule to production memory
        addRuleDirectly(rule);

        // fire all rules, including the new one
        List<Object> result = fireAllRulesNow();

        // remove that rule from production memory
        removeRule(rule);

        // return the result
        return result;
    }

    /**
     * Manually execute and fire all rules
     * @return result containing updated facts or null if rules failed
     */
    public List<Object> fireAllRulesNow() throws Exception{
        // get state full knowledge session first
        StatefulKnowledgeSession session = getSharedStatefulKnowledgeSession();

        // register global messenger for RESTful container and RabbitMQ
        Messenger messenger = Messenger.getInstance();
        session.setGlobal("messenger", messenger);

        // fire all rules
        session.fireAllRules();

        // return content of the container
        return messenger.retrieveRestfulContainer();
    }

    /**
     * Used when stream processing facts
     * @param facts to be processed
     * @return number of executed rules
     */
    public Integer streamProcessFacts(List<MappedFact> facts) {

        // preload facts
        StatefulKnowledgeSession session = loadFacts(facts);

        try {
            // set global if possible
            session.setGlobal("messenger", Messenger.getInstance());
        } catch (Exception ignored) {}

        // execute
        return session.fireAllRules();
    }

    /**
     * Load facts into Working memory, but without rule execution
     * @param facts to be loaded
     * @return session
     */
    public StatefulKnowledgeSession loadFacts(List<MappedFact> facts) {
        // get state full knowledge session first
        StatefulKnowledgeSession session = getSharedStatefulKnowledgeSession();

        // insert every fact into session
        facts.forEach(session::insert);

        return session;
    }

    /**
     * Initialise facts and load them into Working memory
     * @param hibernate client
     */
    private void initialiseFacts(HibernateClient hibernate) throws DatabaseException {
        logger.trace("Loading facts from DB");
        for (Class clazz: PersistedFacts.getListOfPersistedFactClasses()) {

            // get necessary query
            String query = QueryHelper.createListQuery(clazz);

            // get facts
            List facts = hibernate.executeQuery(query);

            // load them into working memory
            if (facts != null && !facts.isEmpty()) {

                String msg = String.format("Loading %d facts into working memory (of %s class)", facts.size(), clazz.getSimpleName());
                logger.trace(msg);

                // log it also to timeline
                FactsLogger.log(msg);

                loadFacts(facts);
            } else {
                logger.trace(String.format("Zero facts (of %s class) retrieved from DB - nothing to load into Working memory", clazz.getSimpleName()));
            }
        }
    }

    /**
     * Initialise rules by loading them into Production memory
     * @param hibernate client
     */
    private void initialiseRules(HibernateClient hibernate) throws RuleException, DatabaseException {
        logger.trace("Loading rules from DB");

        // we are working with Instances here
        String query = QueryHelper.createListQuery(InstanceORM.class);

        // load those rules from DB
        List rules = hibernate.executeQuery(query);

        // load them into production memory
        if (rules != null && !rules.isEmpty()) {
            String msg = String.format("Loading %d rules into production memory ", rules.size());
            logger.trace(msg);

            // log it also to timeline
            RulesLogger.log(msg);

            // load rules into memory
            loadRules(rules);
        } else {
            logger.trace("Zero rules retrieved from DB - nothing to load into Production memory");
        }
    }

    /**
     * Initialise both Working and Production memory
     * @param hibernate client
     */
    public void initialiseWorkingMemory(HibernateClient hibernate) throws RuleException, DatabaseException {

        String msg = "Server start event received, reloading last known state";
        TimelineLogger.log(msg);

        FactsLogger.log(msg);
        initialiseFacts(hibernate);

        RulesLogger.log(msg);
        initialiseRules(hibernate);

        doneWithInitialisation();
    }

    /**
     * Shut down Rule engine
     */
    public static void shutDown() {
        if (singleton != null && singleton.session != null) {
            logger.trace("Shutting down Rule Management");
            singleton.session.halt();
            singleton.session.dispose();
            singleton.session.destroy();

            singleton.session = null;
        }
    }
}
