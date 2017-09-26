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

Service installation & configuration
====================================