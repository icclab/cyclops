package ch.icclab.cyclops.consume.consumers;
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

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.consume.ConsumerEntry;
import ch.icclab.cyclops.consume.RabbitMQListener;
import ch.icclab.cyclops.facts.FactMapping;
import ch.icclab.cyclops.facts.MappedFact;
import ch.icclab.cyclops.rule.RuleManagement;
import ch.icclab.cyclops.util.loggers.StreamLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 14/04/16
 * Description: Fact consumer
 */
public class FactsConsumer extends AbstractConsumer {
    final static Logger logger = LogManager.getLogger(FactsConsumer.class.getName());

    @Override
    protected void consume(String content, ConsumerEntry consumer, Long deliveryTag) {
        // parse supported facts
        List<MappedFact> listOfFacts= FactMapping.fromJson(content);

        if (listOfFacts != null && !listOfFacts.isEmpty()) {
            try {
                // access rule management
                RuleManagement management = RuleManagement.getInstance();

                // load facts to working memory and execute them
                Integer num = management.streamProcessFacts(listOfFacts);

                // everything processed in order, ACK the message
                if (deliveryTag != null) consumer.ackMessage(deliveryTag);

                StreamLogger.log(String.format("%d fact(s) received and %d rules executed", listOfFacts.size(), num));
            } catch (Exception e) {
                // something went wrong, rather NACk the message
                if (deliveryTag != null) consumer.nackMessage(deliveryTag);

                StreamLogger.log(String.format("%d fact(s) received, but rule execution failed: %s", listOfFacts.size(), e.getMessage()));
            }

        } else {
            // ACK this message so it doesn't reappear
            if (deliveryTag != null) consumer.ackMessage(deliveryTag);

            StreamLogger.log("Zero facts loaded, as the received message is corrupted");
        }
    }
}
