#!/bin/bash

# Copyright (c) 2018. SPLab, Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.

# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at

# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

# Author: Dorodko Serhii (dord@zhaw.ch)
# Created: 27.07.2018
# Desription: Rulles apply script for Cyclops demo

# Coin crd broadcast rule
curl -X "POST" "http://localhost:4570/rule" -H "Content-Type: text/plain" -d $'import ch.icclab.cyclops.facts.Charge;
import java.util.List;
global ch.icclab.cyclops.publish.Messenger messenger;

rule "Broadcast CDRs"
salience 20
when
  $charge: List( size > 0 ) from collect ( Charge() )
then
  messenger.broadcast($charge);
  $charge.forEach(c->retract(c));
end'

echo

# Coin billing broadcast rule
curl -X "POST" "http://localhost:4571/rule" -H "Content-Type: text/plain" -d $'import ch.icclab.cyclops.facts.Bill;
import java.util.List;

global ch.icclab.cyclops.publish.Messenger messenger;

rule "Broadcast generated bills"
salience 30
when
  $bills: List(size > 0) from collect (Bill())
then
  // broadcast and remove processed bills
  messenger.broadcast($bills);
  $bills.forEach(bill->retract(bill));
end'

echo

# Coin billing collect rule
curl -X "POST" "http://localhost:4571/rule" -H "Content-Type: text/plain" -d $'import ch.icclab.cyclops.facts.BillRequest;
import ch.icclab.cyclops.facts.Charge;
import ch.icclab.cyclops.facts.Bill;
import java.util.List;

rule "Collect CDRs for the Bill Request"
salience 50
when
  $request: BillRequest($accounts: accounts)
  $CDRs: List(size > 0) from collect (Charge(account memberOf $accounts))
then
  // bills for each currency of account\'s CDRs
  List<Bill> bills = $request.process($CDRs);

  // add bills to the working memory
  bills.forEach(bill->insert(bill));

  // remove processed CDRs and the bill request
  $CDRs.forEach(c->retract(c));
  retract($request);
end'

echo

# Coin cdr pricing rule
curl -X "POST" "http://localhost:4570/rule" -H "Content-Type: text/plain" -d $'import ch.icclab.cyclops.facts.Usage;
import ch.icclab.cyclops.facts.Charge;

rule "Static rule for ram"
salience 50
when
  $usage: Usage(metric == "memory")
then
  Charge charge = new Charge($usage);
  charge.setCharge(0.01 * $usage.getUsage());
  charge.setCurrency("CHF");

  retract($usage);
  insert(charge);
end'

# Coin cdr forecast pricing rule
curl -X "POST" "http://localhost:4570/rule" -H "Content-Type: text/plain" -d $'import ch.icclab.cyclops.facts.Usage;
import ch.icclab.cyclops.facts.Charge;

rule "Test 1 rule for ram"
salience 60
when
  $usage: Usage(metric == "memory" && account.contains("test1"))
then
  Charge charge = new Charge($usage);
  charge.setCharge(0.01 * $usage.getUsage());
  charge.setCurrency("CHF");

  retract($usage);
  insert(charge);
end'
