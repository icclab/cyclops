=======================
Building & Installation
=======================

Cyclops framework is made available as a set of docker images, and full source 
code is available licensed under ASL 2.0.

Building from source
====================

Requirements
------------
You will need following software packages to be installed before you start the 
build and setup process from source files:

+-----------------+--------------------------------+
| Dependencies    | Supported Version              |
+=================+================================+
| Java            | Oracle Java 8 and higher       |
+-----------------+--------------------------------+
| Maven           | 3.0.5 or higher                |
+-----------------+--------------------------------+
| Git             | 2.x.x or higher                |
+-----------------+--------------------------------+

When deploying, additionally you will need the following services installed 
and reachable by the Cyclops framework components.

+-----------------+--------------------------------+
| Dependencies    | Supported Version              |
+=================+================================+
| RabbitMQ        | 3.6                            |
+-----------------+--------------------------------+
| Postgresql      | 9.6                            |
+-----------------+--------------------------------+

Download the source
-------------------
Download the full source code via Git clone
::

  git clone https://github.com/icclab/cyclops.git

Once source code download finishes, check the folder structure and you should 
see all the microservices in their separate subfolders. We will now proceed 
with building every microservice individually.

Building the binaries
---------------------
Cyclops framework comprises of these micro services:
- usage data record generation microservice (udr)
- rating and charging microservice (cdr)
- billing microservice (billing)
- rule engine (coin)

Each one of the above needs to be built individually. Before proceeding with 
the build phase of any component, make sure your *JAVA_HOME* environment 
variable is properly set. On Ubuntu 16.04 machine, this can be normally be 
done through -

::

  source /etc/environment
  export JAVA_HOME="/usr/lib/jvm/java-8-oracle"

Make sure you change the path appropriately.

Building udr
^^^^^^^^^^^^
Change directory to UDR subfolder within *cyclops* folder.
::

  mvn dependency:tree
  mvn package seembly:single
  mv target/cyclops-udr-3.0.0-jar-with-dependencies.jar target/udr.jar

The java binary file is located within *cyclops/UDR/target/* as **udr.jar**

Building cdr
^^^^^^^^^^^^
Change directory to CDR subfolder within *cyclops* folder.
::

  mvn dependency:tree
  mvn package seembly:single
  mv target/cyclops-cdr-3.0.0-jar-with-dependencies.jar target/cdr.jar

The java binary file is located within *cyclops/CDR/target/* as **cdr.jar**

Building billing
^^^^^^^^^^^^^^^^
Change directory to Billing subfolder within *cyclops* folder.
::

  mvn dependency:tree
  mvn package seembly:single
  mv target/cyclops-billing-3.0.0-jar-with-dependencies.jar target/billing.jar

The java binary file is located within *cyclops/Billing/target/* as 
**billing.jar**

Building rule-engine (coin)
^^^^^^^^^^^^^^^^^^^^^^^^^^^
Change directory to Coin subfolder within *cyclops* folder.
::

  mvn dependency:tree
  mvn package seembly:single
  mv target/cyclops-coin-1.1-jar-with-dependencies.jar target/coin.jar

The java binary file is located within *cyclops/Coin/target/* as **coin.jar**

Service installation & configuration
====================================
Now that you have successfully compiled and built binaries of each individual 
Cyclops components, let us understand how to properly install and configure 
them.

**Assumption: An Ubuntu 16.04 OS is installed on nodes where Cyclops framework services will be executed**

Installing and configuring UDR service
--------------------------------------

Installing and configuring CDR service
--------------------------------------

Installing and configuring Billing service
------------------------------------------

Installing and configuring Rule-engine (Coin) service
-----------------------------------------------------




