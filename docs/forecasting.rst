*********************************
Forecasting and Estimation Engine
*********************************

Description of the implementation of the forecasting and estimation engine and
how it can be used to create cost forecasts and evaluate different pricing
models.

Per account forecast: (Forecast command)
########################################

* All historical records for account are retrieved from DB
* They are grouped by usage type
* A set of forecast records are generated for each usage type using the ARIMA model, UDRs, CDRs and Bills are generated using ‘evaluation rules’

Model based global forecast: (Forecast command with no account specified)
#########################################################################

* Same as per account forecast, but ignores account and aggregates all records
* Depends on the ARIMA model itself to determine usage and account activity patterns and customize the forecast

2D pattern based forecast: (GlobalForecast command)
###################################################

* Creates usage patterns by grouping the historical records by account and by type
* Creates an activity pattern by counting how many users were active for each day in the history
* Uses the ARIMA model to create a future activity forecast (how many accounts are expected to be active each day in the forecast period)
* For each active account for each day in the forecast period, assigns one of the generated usage patterns and uses ARIMA model to forecast the usage for each usage type
* As before, UDRs, CDRs and Bills are generated using ‘evaluation rules’


Evaluation rules/Pricing models under evaluation:
#################################################

* Rules that are fired only when the records have a specific tag
* Groups of rules can have the same tag target so that they can be grouped into a separate pricing model to be evaluated
* The pricing model is marked by its tag, and the target model is specified by its tag in the forecast command payload

**Request example (can be used as a payload either to the command HTTP endpoint
or to send a command through AMPQ:**

::

  {

     "command": "GlobalForecast",

     "target": "test1",

     "forecastSize": 15

  }

**Rule example:**

::

  rule "Test 1 rule for ram (12:3:58.4 11/Jun/2019)"

  salience 60

  when

     $usage: Usage(metric == "memory" && account.contains("test1"))

  then

  …

**The important things to note about the rules are:**

* The salience must be higher than the ‘real’ rules, so that it gets checked first, or it will never be triggered. This will not affect ‘real’ records, as they will not be tagged.
* The tag for this rule is ‘test1’. The forecast generator tags the records it creates in the account and data fields, so the rule can look for the tag in either place. In this example, for readability, the rule checks for its tag in the account field. In a real case, it is safer to check the data field for the "target":"test1" pair. That excludes the possibility that real records from a user named 'test1' will fire the test rules.
* Rules with the same tag (as long as they comply with the above two points) can be targeted to evaluate a whole new pricing model (a set of charging and pricing rules in this context)
