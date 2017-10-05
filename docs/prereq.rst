==================
Preparing the host
==================
Now that you have successfully compiled and built binaries of each individual 
Cyclops components, let us understand how to properly install and configure 
them.

**Assumption: An Ubuntu 16.04 OS is installed on nodes where Cyclops framework services will be executed**

A few recommended housekeeping steps are receommended before actually starting 
with the individual service configurations.

Adding a dedicated user
-----------------------
Since cyclops services will be executed as system processes, it is highly 
recommended to create a dedicated user to execute these services.

::

  sudo useradd -s /sbin/nologin cyclops

Optional softwares
------------------
As cyclops framework components generate extensive log messages, it is highly 
recommended to setup **logrotate** process to ensure log files do not consume 
the entire usable disk space.

**cURL** is used to setup RabbitMQ bindings for various Cyclops services later 
on. It is recommended to install it to avoide setup using the graphical 
interface.

System folders
--------------
Lets set up appropriate directories to place the binaries, configuration 
files, and log files.

::

  sudo mkdir -p /var/log/cyclops/
  sudo mkdir -p /etc/cyclops/
  sudo mkdir -p /usr/local/bin/cyclops/
  sudo mkdir -p /var/lib/cyclops/

We will move the compiled binaries into */usr/local/bin/cyclops/* subtree, 
configuration files under */etc/cyclops/* subtree, and the log files will be 
stored within */var/log/cyclops/* directory subtree. */var/lib/cyclops/* is 
used in case the services require a folder to store additional files.

Bootstrapping Postgresql
------------------------
Please use the following statements to allow Cyclops micro-services to setup 
service specific tables.

::

  sudo -i -u postgres psql -c "alter system set idle_in_transaction_session_timeout='5min';"
  sudo -i -u postgres psql -c "DROP USER IF EXISTS cyclops;"
  sudo -i -u postgres psql -c "CREATE USER cyclops WITH PASSWORD 'pass1234';"
  sudo -i -u postgres psql -c "ALTER USER cyclops CREATEDB;"
  sudo -i -u postgres psql -c "CREATE DATABASE cyclops;"
  sudo -i -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE cyclops TO cyclops;"

**Please set a reasonably strong password while creating a Cyclops DB user**

Configuring RabbitMQ
--------------------
Since Cyclops services uses RabbitMQ for inter-process communication, it is 
important that the messaging system is already preconfigured to enable 
communication.

::

  sudo rabbitmq-plugins enable rabbitmq_management
  sudo rabbitmqctl add_user cyclops pass1234
  sudo rabbitmqctl set_user_tags cyclops administrator
  sudo rabbitmqctl add_vhost cyclops
  sudo rabbitmqctl set_permissions -p cyclops cyclops ".*" ".*" ".*"

**Please set a reasonably strong rabbitmq user password**

For sake of ease, we will continue using *pass1234* in subsequent pages, do 
replace it with the actual value that was used instead.

.. figure:: rabbit_scheme.png
    :width: 800px
    :align: center
    :alt: global binding schema
    :figclass: align-center

Follow through the guide for installing each service individually.