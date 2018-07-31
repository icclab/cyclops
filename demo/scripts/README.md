# Scripts usage
This section will show how to run script to generate custom usage data and produce example inctance of bill.

## Demo Scenario Description

The scripts included generates synthetic data stream for a fixed user account in a hypothetical Kubernetes cluster. The script generates configured number of usage data points tracking pod memory consumption over the specified time period. The usage value is randomized number between 0 and 2048 MB.

Each synthetic usage data value that the generator script creates has the following example JSON outline:

```json
 {
 	"metric": "memory",
 	"account": "dord",
 	"time": 1493450936000,
 	"usage": 22560,
 	"unit": "KB",
 	"data":{
 		"pod_id":"string id"
 	}   
 }
```

Specifically, the released code send values as MB instead of KB. 

### Rules
As cyclops framework is rules driven, pricing and billing models have to be injected before meaningful processing can be done by the framework. `rulesApply.sh` script registers all necessary rules in Cyclops, including pricing rules, which defines how the consumed resources metrics are charged.
```
import ch.icclab.cyclops.facts.Usage;
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
```
As can be seen by the rule definition above, memory parameter is charged as a rate of 0.01 CHF per MB. This is a purely for demonostration purposes. In realistic scenarios, first you will have to transform memory levels into MB-hr format or MB-month value. Based on how the inter-sample arrival window has been configured, the computation will happen accordingly (BUT NOT FOR THIS DEMO THOUGH). 

To execute pricing and billing rule registration with Cyclops, simply execute the following command.

```sh
$ bash rulesApply.sh
```
### Data simulation
To generate usage data there is `dataGeneration.sh` script which allows you to specify some window (in minutes) for which usage data points should be generated as part of simulation. You will also need to specify the inter-arrival duration between samples. This script can be executed as shown below. During execution, it will generate and send simulated usage data stream into Cyclop framework. It simulates data, that would be collected from real clusters periodicaly. The command is provided next:   

```sh
$ bash dataGeneration.sh -t0=22-Sep-2017 -t1=23-Sep-2017 -i=360
```
- t0 = start of the data simulation window (a calendar date)
- t1 = end of the data simulation window (a calendar date)
- i = inter sample arrival interval (in minutes)

Upon termination, necessary simulation data has been fed into Cyclops and we are ready to start the next phases: generation of usage reports, charge data records and finally a bill object.

### Invoice producing 

To produce invoice, script runs few commands in a row:
 - GenerateUDRs
 - FlushUDRs
 - GenerateBill

User has to specify time window as well. Cyclops will generate invoice for this particular time. `getInvoice.sh` script accepts edge time values like in data simulation part but time interval is not needed here. Instead customer has to define deley between commands in seconds. 

```sh
$ bash getInvoice.sh -t0=22-Sep-2017 -t1=23-Sep-2017 -d=5
```
### Database cleaning
There is also script to delete all queries from database, so it's easier to play with demo. `cleanDB.sh` can delete just usage, udr, cdr, bills(`-d` | `--data`) or rulles(`-r` | `--rules`) or both(`-r -d`). 
```sh
bash cleanDB.sh --rules
bash cleanDB.sh --data
bash cleanDB.sh -d -r
```

 






