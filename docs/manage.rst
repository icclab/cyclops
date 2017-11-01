================
Managing Cyclops
================

Now that we have the framework configured properly, lets look at how to manage 
a live Cyclops service.

Configuring a collector
=======================

Rules management
================

Managing rules in coincdr
-------------------------
Cyclops data transformation workflow is heavily guide by pricing and billing 
models injected within the rule engines attached to the microservices *cdr* 
and *billing*. These are called **coincdr** and **coinbill**.

Assuming that the usage data being sent to cyclops has the following form -

::

  {
    "metric":"somemeter",
    "account":"customer-account",
    "usage":2,
    "unit":"GB",
    "time":1507593601000,
    "data":{
      "serviceId":"user1@cust-x.ch",
      "billingModel":"Smart"
    }
  }

You can inject rules within the **coincdr** rule engine to manipulate any 
fields you see in the JSON above. Fields inside the *data* block is accessible 
via the corresponding Map object.

A sample rule is shown below -

::

  import ch.icclab.cyclops.facts.Usage;
  import ch.icclab.cyclops.facts.Charge;
  
  rule "Rate somemeter usage value"
  salience 50
  when
    $usage: Usage(metric == "somemeter" && data != null && data contains "billingModel" && data["billingModel"]=="Smart")
  then
    Charge charge = new Charge($usage);
    charge.setCharge($usage.getUsage() * 0.4);
    
    insert(charge);
    retract($usage);
  end

Analyzing the rule above, if the usage record being processed contains a data 
block and an element *billingModel*, then generates the charge by multiplying 
the **usage** value with **0.4**.

This example simply shows how with ease, Cyclops rule engines can be 
programmed.

You can have multiple rules which can be potentially apply in a given 
situation, but which one is triggered can be controlled by the weight assigned 
to a rule. The weight is controlled via the **salience** parameter. 

Lets assume one wishes to have a catch all rule for processing usage. This can 
be written as shown below -

::

  import ch.icclab.cyclops.facts.Usage;
  import ch.icclab.cyclops.facts.Charge;

  rule "Remaining services for free"
  salience 40
  when
    $usage: Usage()
  then
    Charge charge = new Charge($usage);
    charge.setCharge(0);
  
    insert(charge);
    retract($usage);
  end

Since the *salience* of the rule is lesser than the first rule, it will be 
applied only when the first rule mentioned in this page is inapplicable.

You can even control data transmission behavior via rules. Say we want to push 
all generated charge records over to a channel, specially within the Cyclops 
framework we must push the cdr records to a specific RabbitMQ exchange, it can 
be achieved via the following rule within *coincdr*.

::

  import ch.icclab.cyclops.facts.Charge;
  import java.util.List;
  global ch.icclab.cyclops.publish.Messenger messenger;

  rule "Broadcast CDRs"
  salience 20
  when
    $charge: List( size > 0 ) from collect ( Charge() )
  then
    messenger.broadcast($charge);
    $charge.forEach(c->retract(c));
  end

Managing rules in coinbill
--------------------------
Just like the rules for *coincdr* that governs the transformation of udr 
records to cdr records, one needs to manage the rules in coinbill to govern 
the generation of bill from cdr records.

Lets look at a sample *coinbill* rule that upon receipt of the bill generation 
command and the list of cdr records, creates the bill for the requested set of 
accounts - 

::

  import ch.icclab.cyclops.facts.BillRequest;
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
  end

The statements of the rule above should be self explanatory. Similar to 
*coincdr* where one had to prepare a rule for sending the generated records to 
next stop in the data path, here too in Cyclops framework, the generated bill 
records should be moved to the next stage in the messaging setup -

::

  import ch.icclab.cyclops.facts.DatonusBill;
  import java.util.List;

  global ch.icclab.cyclops.publish.Messenger messenger;

  rule "Broadcast generated Datonus bills"
  salience 30
  when
    $bills: List(size > 0) from collect (DatonusBill())
  then
    // broadcast and remove processed bills
    messenger.broadcast($bills);
    $bills.forEach(bill->retract(bill));
  end

As you can notice, usually all Java language constructs and objects are available to you while formulating a rule.

Rule management endpoints
-------------------------
The above shown example rules and any other that one may create must be 
uploaded to the corresponding rule engines. This is achieved by sending a HTTP 
POST request to the rule engine endpoint

- coin-cdr-url-or-ip:port/rule
- coin-bill-url-or-ip:port/rule

Generation of a bill
====================
