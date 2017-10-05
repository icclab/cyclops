================================
Install & Configure: Rule Engine
================================

Coin is our rule engine and microservice enabling model insertion and its 
execution. It supports the cdr and billing processes in model based pricing, 
charging and discounting workflows.

Two rule engine processes have to be setup for each of the following microservices -

- cdr
- billing

These two rule engine processes in this guide are therefore named as follows -

- coincdr
- coinbill

Setup & configuration: coincdr
------------------------------

Preparing the host machine
^^^^^^^^^^^^^^^^^^^^^^^^^^
Start by creating system folders for coincdr service.

::

  sudo mkdir -p /var/log/cyclops/coincdr/
  sudo mkdir -p /etc/cyclops/coincdr/
  sudo mkdir -p /usr/local/bin/cyclops/coincdr/

For logging to work properly, these files must exist, perform the next 
commands to ensure the same.

- errors.log
- trace.log
- hibernate.log
- facts.log
- rules.log
- timeline.log
- dispatch.log
- stream.log

::

  sudo touch /var/log/cyclops/coincdr/errors.log
  sudo touch /var/log/cyclops/coincdr/trace.log
  sudo touch /var/log/cyclops/coincdr/hibernate.log
  sudo touch /var/log/cyclops/coincdr/facts.log
  sudo touch /var/log/cyclops/coincdr/rules.log
  sudo touch /var/log/cyclops/coincdr/timeline.log
  sudo touch /var/log/cyclops/coincdr/dispatch.log
  sudo touch /var/log/cyclops/coincdr/stream.log

Let's move the binary and the configuration files from the compiled locations 
to the target system destinations.

::

  sudo cp Coin/target/coin.jar /usr/local/bin/cyclops/coincdr/coincdr.jar
  sudo cp CDR/config/coin.conf /etc/cyclops/coincdr/coincdr.conf

Preparing RabbitMQ
^^^^^^^^^^^^^^^^^^
Assuming that RabbitMQ is running on the same machine where the following 
commands are to be executed, running these will setup necessary exchanges, 
queues and bindings between them for coincdr process to function properly.

Configuring coincdr
^^^^^^^^^^^^^^^^^^^

Fixing permissions
^^^^^^^^^^^^^^^^^^
Before running any of the Cyclops framework services via *systemctl* command, 
make sure that the process user *cyclops* which was created earlier to run the 
process has full read/write access to Cyclops specific system folder and files.

::

  sudo chown -R cyclops:cyclops /var/log/cyclops/
  sudo chown -R cyclops:cyclops /usr/local/bin/cyclops/
  sudo chown -R cyclops:cyclops /etc/cyclops/
  sudo chown -R cyclops:cyclops /var/lib/cyclops/

Setup as a service
^^^^^^^^^^^^^^^^^^
Create a file called *cyclops-coincdr.service* in */etc/systemd/system/* 
directory. Add the following content to this file:

::

  [Unit]
  Description=Cyclops Coin CDR Service
  After=network.target rabbitmq-server.service postgresql-9.6.service
  
  [Service]
  ExecStartPre=/bin/sleep 2
  Type=simple
  User=cyclops
  ExecStart=/usr/bin/java -jar /usr/local/bin/cyclops/coincdr/coincdr.jar /etc/cyclops/coincdr/coincdr.conf
  Restart=on-abort
  
  [Install]
  WantedBy=multi-user.target

This assumes that the rabbitmq and postgres server is running in the same 
machine where you are setting up coincdr service. If not then remove them from 
the dependencies list by changing the **After** line above. *Do make sure that 
these services are running and reachable before coincdr service is started*.

You can enable and manage the coincdr service and start it by using the 
following systemctl commands.

::

  sudo systemctl enable cyclops-coincdr.service
  sudo systemctl start/stop/restart/status cyclops-coincdr.service

Setup & configuration: coinbill
-------------------------------

Preparing the host machine
^^^^^^^^^^^^^^^^^^^^^^^^^^
Start by creating system folders for coinbill service.

::

  sudo mkdir -p /var/log/cyclops/coinbill/
  sudo mkdir -p /etc/cyclops/coinbill/
  sudo mkdir -p /usr/local/bin/cyclops/coinbill/

For logging to work properly, these files must exist, perform the next 
commands to ensure the same.

- errors.log
- trace.log
- hibernate.log
- facts.log
- rules.log
- timeline.log
- dispatch.log
- stream.log

::

  sudo touch /var/log/cyclops/coinbill/errors.log
  sudo touch /var/log/cyclops/coinbill/trace.log
  sudo touch /var/log/cyclops/coinbill/hibernate.log
  sudo touch /var/log/cyclops/coinbill/facts.log
  sudo touch /var/log/cyclops/coinbill/rules.log
  sudo touch /var/log/cyclops/coinbill/timeline.log
  sudo touch /var/log/cyclops/coinbill/dispatch.log
  sudo touch /var/log/cyclops/coinbill/stream.log

Let's move the binary and the configuration files from the compiled locations 
to the target system destinations.

::

  sudo mv Coin/target/coin.jar /usr/local/bin/cyclops/coinbill/coinbill.jar
  sudo mv CDR/config/coin.conf /etc/cyclops/coinbill/coinbill.conf

Preparing RabbitMQ
^^^^^^^^^^^^^^^^^^
Assuming that RabbitMQ is running on the same machine where the following 
commands are to be executed, running these will setup necessary exchanges, 
queues and bindings between them for coincdr process to function properly.

Configuring coinbill
^^^^^^^^^^^^^^^^^^^

Fixing permissions
^^^^^^^^^^^^^^^^^^
Before running any of the Cyclops framework services via *systemctl* command, 
make sure that the process user *cyclops* which was created earlier to run the 
process has full read/write access to Cyclops specific system folder and files.

::

  sudo chown -R cyclops:cyclops /var/log/cyclops/
  sudo chown -R cyclops:cyclops /usr/local/bin/cyclops/
  sudo chown -R cyclops:cyclops /etc/cyclops/
  sudo chown -R cyclops:cyclops /var/lib/cyclops/

Setup as a service
^^^^^^^^^^^^^^^^^^
Create a file called *cyclops-coinbill.service* in */etc/systemd/system/* 
directory. Add the following content to this file:

::

  [Unit]
  Description=Cyclops Coin Bill Service
  After=network.target rabbitmq-server.service postgresql-9.6.service
  
  [Service]
  ExecStartPre=/bin/sleep 2
  Type=simple
  User=cyclops
  ExecStart=/usr/bin/java -jar /usr/local/bin/cyclops/coinbill/coinbill.jar /etc/cyclops/coinbill/coinbill.conf
  Restart=on-abort
  
  [Install]
  WantedBy=multi-user.target

This assumes that the rabbitmq and postgres server is running in the same 
machine where you are setting up coinbill service. If not then remove them 
from the dependencies list by changing the **After** line above. *Do make sure 
that these services are running and reachable before coincdr service is 
started*.

You can enable and manage the coinbill service and start it by using the 
following systemctl commands.

::

  sudo systemctl enable cyclops-coinbill.service
  sudo systemctl start/stop/restart/status cyclops-coinbill.service
