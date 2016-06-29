<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

## Openstack collector 
Usage data collection from your Openstack deployment.

### Installation
The Openstack usage collector requires Java 8 as well as RabbitMQ, both of which can be installed using the following <a href="https://github.com/icclab/cyclops/wiki/Dependencies" target="_blank">guide</a>. 

Openstack collector also requires an access to rational database. Even though you can deploy and use any backend database, the default database is PostgreSQL, which you can install and set up with

    cd install
    bash install_postgresql.sh
    bash create_database.sh

### Configuration
For Openstack collector to function properly you will need to edit <code>config/openstack_event.conf</code> file and specify your Openstack evetns settings, as well as RabbitMQ and Hibernate connection settings, both of which can be accessed remotely.

### Set up bindings
Before starting Openstack usage collection it is paramount to bind its exchanges to the RCB Cyclops queues as well as Cyclops queue to the nova exchange. In order to do so simply run the following script and provide server's location and port RabbitMQ admin is listening on:

    bash install/setup_bindings.sh localhost 15672

### Logging
To have the logging system enabled you need to run the following:

    bash install/setup_logging.sh
  
Then all Openstack collector logs will be stored in <code>/var/log/cyclops/openstack_events/</code> directory.

### Run the JAR file
In order to run Openstack event collector as embedded JAR you need to have Java 8 installed and execute:

    java -jar bin/openstack_event.jar config/openstack_event.conf 
  
### Compile from the source code
If you want to compile the code on your own and your environment already has Java 8 and Maven 3 present, simply execute the following commands:

    cd scripts
    bash compile.sh
  
You will find the compiled JAR embedded file in the <code>bin</code> subdirectory.
  
### Documentation
You can find the API reference guide as well as troubleshooting page in this <a href="https://github.com/icclab/cyclops/wiki/OpenStack-Events" target="_blank">Wiki section.</a>

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
