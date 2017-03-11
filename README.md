<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

# Cyclops: Rating, Charging and Billing framework
Cyclops is a comprehensive dynamic rating-charging and billing solution for cloud services and beyond.

<a href="https://blog.zhaw.ch/icclab/files/2016/06/Architecture_without_logo.png" target="_blank"><img src="https://blog.zhaw.ch/icclab/files/2016/06/Architecture_without_logo.png" alt="Architecture" width="620"></img></a>

### Core components
The RCB Cyclops framework is a collection of these core micro services:

  - UDR micro service - usage calculation, persistence and exposure
  - RC micro service  - charge generation and meter rating
  - Billing micro service - invoice generation

### Usage collectors
The current release includes the following usage collectors (with many more to come)
  
  - CloudStack
  - OpenStack Events
  - OpenStack Ceilometer

### Supporting components
On top of core micro services the RCB Cyclops frameworks utilises couple of other modules:
  
  - Dashboard - visualisation and forecasting
  - Static rating - used in RC micro service as a rating function
  - Autoscaler - autonomously scaling in and out (in the future release)
  - Load balancer - forwarding requests to micro services (in the future release)

## Deployment
All micro services are written in Java and are embedded so you can easily deploy them in containers or run them locally.

#### Prerequisites
Depending on the configuration you will run, the RCB Cyclops has the following dependencies:

  - Java 8
  - Maven 3
  - InfluxDB 1.0
  - RabbitMQ 3.6
  
All of which you can install one by one from <code>install</code> subfolder or by executing the following command:

    bash install_dependencies.sh

#### Configuration
By default everything is set to run on your local machine, so if you want to access a remote server (for InfluxDB or RabbitMQ), please navigate into respective subfolders and consult Readme files.

#### Logging
All micro services properly log their execution, as well as data coming in and out. In order to set up your environment to include mentioned log files run the following command:

    bash setup_logging.sh

Those will be stored in <code>/var/log/cyclops/</code> folder.

#### Run embedded
All the micro services follow the same way of execution, where you can simply invoke the following command:

    java -jar microservice.jar configuration.conf [port]

You will find each micro service precompiled as an embedded JAR file inside of <code>bin</code> subdirectories. However, if you wish to start all the core micro services with their default configuration files, just execute:

    bash start_cyclops.sh
  
Which will run all micro services in *nohup* background mode, allowing you to close the terminal.

To make sure everything is running correctly either consult <code>nohup.out</code> console output or logs stored in <code>/var/log/cyclops/</code>.

In order to terminate micro services just run the following command:

    bash stop_cyclops.sh

#### Usage collection
Even though couple of usage collectors are included, you can write your own (in any language) and let RCB Cyclops take care of the rest. A detailed explanation is offered in Wiki section, where you will find how the whole system functions, what format is required, how micro services communicate with each other, as well as what to keep in mind.

If you decide to use one of the available usage collection micro services, simply navigate into that respective subfolder and follow the <code>Readme</code>.

#### Compile from source
If you've changed the source code, or you simply cannot use precompiled binary files (which were made available for Ubuntu/Debian) you can easily recompile everything. To do so, please navigate into respective subfolders and either use <code>scripts/compile.sh</code> script or execute the following:

    mvn dependency:tree
    mvn package assembly:single
  
### Documentation
Comprehensive API reference and developer's guides, as well as troubleshooting pages are available in the Wiki section.

### Communication
  * Email: icclab-rcb-cyclops[at]dornbirn[dot]zhaw[dot]ch
  * Website: <a href="http://icclab.github.io/cyclops" target="_blank">icclab.github.io/cyclops</a>
  * Blog: <a href="http://blog.zhaw.ch/icclab" target="_blank">http://blog.zhaw.ch/icclab</a>
  * Tweet us @<a href="https://twitter.com/rcb_cyclops" target="_blank">RCB_Cyclops</a>
   
### Developed @
<img src="https://blog.zhaw.ch/icclab/files/2016/03/cropped-service_engineering_logo_zhawblue_banner.jpg" alt="ICC Lab" height="180" width="620"></img>

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
