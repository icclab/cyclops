================
Managing Cyclops
================

Now that we have the framework configured properly, lets look at how to manage 
a live Cyclops service.

Configuring a collector
-----------------------


Managing rules
--------------
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
applied only when the first rule mentioned in this page is unapplicable.

You can even control data transmission behavior via rules. Say we want to push 
all generated charge records over to a channel, specically within the Cyclops 
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

Generation of a bill
--------------------