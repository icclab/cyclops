<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

## Rule engine micro service
This micro service (aka Coin) is a generic business rule system that is internally utilising <a href="http://drools.org" target="_blank">Drools</a>, an inference engine, for its rule execution. The micro service itself exposes template instantiation, as well as truth maintenance over RESTful APIs, and therefore allow developers to write their own business rules and provide graphical user interfaces for people from sales to work with.

The most prominent features:

 - Rule templates and their instantiation
 - Rule instances and their execution
 - Stateless and stateful sessions
 - ORM mapping using Hibernate
 - Custom and generic fact types
 - Facts parsing and recognition
 - Counters and helper objects
 - Sync and Async messages
 - Output controlled by rules
 - Facts persisted via JTA
 - Logging and auditing
 - SDK for developers
 - One time execution
 - Stream processing
 - Alarm notifications
 - Batch processing
 - Easy debugging 
 - State recovery

### Logging
To have the logging system enabled you need to run the following:

    ./scripts/logging.sh
  
Then all Rule engine service logs will be stored in <code>/var/log/cyclops/coin/</code> directory.

### Run the JAR file
In order to run Rule ngine micro service as embedded JAR you need to have Java 8 installed and execute:

    java -jar bin/coin.jar config/coin.conf [port]
  
If you don't want to use the default port from configuration file, you can optionally specify one by adding it as parameter.

### Compile from the source code
If you want to compile the code on your own and your environment already has Java 8 and Maven 3 present, simply execute the following commands:

    cd scripts
    ./compile.sh
  
You will find the compiled JAR embedded file in the <code>bin</code> subdirectory.

### Scripting
If you decide to automate deployment of the Rule engine micro service, please note that it takes one parameter as path to your configuration file, and optionally specified HTTP port number. The return codes are as follows:

 - 0 - OK
 - 1 - Help
 - 2 - Mismatch in parameters
 - 3 - Corrupted configuration file
 - 4 - Wrong Hibernate credentials
 - 5 - Hibernate failed on pre-loading data
 - 6 - Router couldn't be created
 - 7 - Server failed to start

### Documentation
For API reference guide, as well as documentation please consult <code>documentation</code> folder.

### License
 
      Licensed under the Apache License, Version 2.0 (the "License"); you may
      not use this file except in compliance with the License. You may obtain
      a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations
      under the License.