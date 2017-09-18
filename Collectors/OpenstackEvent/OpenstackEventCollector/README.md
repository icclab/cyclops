<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

## OpenStack Event micro service 
OpenStack Event Collector micro service as part of RCB Cyclops.

### Configuration
For Event Collector micro service to function properly you will need to edit <code>config/openstack_event_collecot.conf</code> file and specify PostgreSQL and RabbitMQ credentials, both of which can be accessed remotely. You can also specify on which port server should be listening by either changing the configuration or by providing port selection as the second console parameter.

### Logging
To have the logging system enabled you need to run the following:

    ./install/setup_logging.sh
  
Then all Event Collector micro service logs will be stored in <code>/var/log/cyclops/event_collector/</code> directory.

### Run the JAR file
In order to run Event Collector micro service as embedded JAR you need to have Java 8 installed and execute:

    java -jar bin/cyclops_event_collector.jar config/cyclops_event_collector.conf [port]
  
If you don't want to use the default port from configuration file, you can optionally specify one by adding it as parameter.

### Compile from the source code
If you want to compile the code on your own and your environment already has Java 8 and Maven 3 present, simply execute the following commands:

    cd scripts
    ./compile.sh
  
You will find the compiled JAR embedded file in the <code>bin</code> subdirectory.
  
### Documentation
You can find the API reference and developer's guide, as well as troubleshooting page in the Wiki section.