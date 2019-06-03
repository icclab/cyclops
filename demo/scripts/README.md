# Scripts usage
This section will show how to run script to generate custom usage data and produce example inctance of bill.
### Rules
Before starting there are rules to be applyed. `rulesApply.sh` script applys all necessary ruules to Cyclops, including pricing rule, which defines billing policy:
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
In this demo case, biiling occurs basing on memory. 
```sh
$ bash rulesApply.sh
```
### Data simulation
To generate usage data there is `dataGeneration.sh` script which allows to specify usage time scope generation and time window in minutes. After running, it will generate and feed usage to Cyclops. It simulates data, that would be collected from real cluster in specifyed interval so the timestamp is appropriate.   

```sh
$ bash dataGeneration.sh -t0=22-Sep-2017 -t1=23-Sep-2017 -i=360
```
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
 
 






