=======================
Building & Installation
=======================

Cyclops framework is made available as a set of docker images, and full source code is 
available licensed under ASL 2.0.

Building from source
====================

Requirements
------------
You will need following software packages to be installed before you start the build and 
setup process from source files:

+-----------------+--------------------------------+
| Dependencies    | Supported Version              |
+=================+================================+
| Java            | Oracle Java 8 and higher       |
+-----------------+--------------------------------+
| Maven           | 3.0.5 or higher                |
+-----------------+--------------------------------+
| Git             | 2.x.x or higher                |
+-----------------+--------------------------------+

When deploying, additionally you will need the following services installed and 
reachable by the Cyclops framework components.

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

Once source code download finishes, check the folder structure and you should see all 
the microservices in their separate subfolders. We will now proceed with building every 
microservice individually.

Building the binaries
---------------------
Cyclops framework comprises of these micro services:
- usage data record generation microservice (udr)
- rating and charging microservice (cdr)
- billing microservice (billing)
- rule engine (coin)

Each one of the above needs to be built individually. Before proceeding with the build 
phase of any component, make sure your *JAVA_HOME* environment variable is properly set. 
On Ubuntu 16.04 machine, this can be done through -

::

  source /etc/environment
  export JAVA_HOME="/usr/lib/jvm/java-8-oracle"

Building udr
^^^^^^^^^^^^
Change directory to UDR subfolder within *cyclops* folder.
::

  mvn dependency:tree
  mvn package seembly:single
  mv target/cyclops-udr-3.0.0-jar-with-dependencies.jar target/udr.jar

The java binary file is located within *cyclops/UDR/target/* as *udr.jar*

Service installation & configuration
====================================





