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

Generation of a bill
--------------------