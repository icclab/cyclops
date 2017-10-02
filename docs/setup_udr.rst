========================
Install & Configure: UDR
========================

Let us setup UDR micro-service to run as a linux system service.

Preparing the host machine
--------------------------
Start by creating system folders for UDR service.

::

  sudo mkdir -p /var/log/cyclops/udr/
  sudo mkdir -p /etc/cyclops/udr/
  sudo mkdir -p /usr/local/bin/cyclops/udr/

For logging to work properly, these files must exist, perform the next 
commands to ensure the same.

- errors.log
- trace.log
- rest.log
- dispatch.log
- data.log
- commands.log
- timeseries.log

::

  sudo touch /var/log/cyclops/udr/errors.log
  sudo touch /var/log/cyclops/udr/trace.log
  sudo touch /var/log/cyclops/udr/rest.log
  sudo touch /var/log/cyclops/udr/dispatch.log
  sudo touch /var/log/cyclops/udr/data.log
  sudo touch /var/log/cyclops/udr/commands.log
  sudo touch /var/log/cyclops/udr/timeseries.log

Let's move the binary and the configuration files from the compiled locations 
to the target system destinations.

::

  sudo mv UDR/target/udr.jar /usr/local/bin/cyclops/udr/
  sudo mv UDR/config/udr.conf /etc/cyclops/udr/

Preparing the Postgressql / TimescaleDB
---------------------------------------
Before working with the udr service, it is necessary to setup the appropriate database and table schemas. This can be achieved by executing the following commands on the host where the Postgresql service is running.

::

  psql -U postgres -h localhost <<EOF
  CREATE DATABASE cyclops_udr WITH OWNER cyclops;
  GRANT ALL PRIVILEGES ON DATABASE cyclops_udr TO cyclops;
  EOF

::

  psql -U cyclops -h localhost -d cyclops_udr <<EOF
  CREATE TABLE IF NOT EXISTS usage (
    time      TIMESTAMP         NOT NULL,
    metric    TEXT              NOT NULL,
    account   TEXT              NOT NULL,
    usage     DOUBLE PRECISION  NOT NULL,
    data      JSONB,
    unit      TEXT
  );
  CREATE INDEX IF NOT EXISTS usage_metric ON usage (metric, time DESC);
  CREATE INDEX IF NOT EXISTS usage_account ON usage (account, time DESC);
  CREATE INDEX IF NOT EXISTS usage_unit ON usage (unit, time DESC);
  CREATE INDEX IF NOT EXISTS usage_data ON usage USING HASH (data);
  EOF

::

  psql -U cyclops -h localhost -d cyclops_udr <<EOF
  CREATE TABLE IF NOT EXISTS udr (
    time_from TIMESTAMP         NOT NULL,
    time_to   TIMESTAMP         NOT NULL,
    metric    TEXT              NOT NULL,
    account   TEXT              NOT NULL,
    usage     DOUBLE PRECISION  NOT NULL,
    data      JSONB,
    unit      TEXT
  );
  CREATE INDEX IF NOT EXISTS udr_metric ON udr (metric, time_from DESC);
  CREATE INDEX IF NOT EXISTS udr_account ON udr (account, time_from DESC);
  CREATE INDEX IF NOT EXISTS udr_unit ON udr (unit, time_from DESC);
  CREATE INDEX IF NOT EXISTS udr_data ON udr USING HASH (data);
  EOF

Preparing RabbitMQ
------------------
Assuming that RabbitMQ is running on the same machine where the following 
commands are to be executed, running these will setup necessary exchanges, 
queues and bindings between them for udr process to function properly.

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.udr.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.udr.commands

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.udr.broadcast

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.udr.dispatch

In the above commands, do not forget to replace *cyclops* and *pass1234* to 
correct RabbitMQ user/pass values that was setup earlier.

Configuring UDR
---------------
You can configure the service endpoints and dependencies in the configuration 
file located under */etc/cyclops/udr/*

Default content is shown next:
::

  # HTTP and/or HTTPS port to be exposed at
  ServerHTTPPort=4567
  #ServerHTTPSPort=5567
  #ServerHTTPSCertPath=/path/to/cert.p12
  #ServerHTTPSPassword=password
  
  # Health check every X seconds
  ServerHealthCheck=30
  ServerHealthShutdown=false
  
  # Database credentials to TimescaleDB
  DatabasePort=5432
  DatabaseHost=localhost
  DatabaseUsername=cyclops
  DatabasePassword=password
  DatabaseName=cyclops_udr
  DatabasePageLimit=500
  DatabaseConnections=4
  
  # Publisher (RabbitMQ) credentials
  PublisherHost=localhost
  PublisherUsername=cyclops
  PublisherPassword=password
  PublisherPort=5672
  PublisherVirtualHost=cyclops
  PublisherDispatchExchange=cyclops.udr.dispatch
  PublisherBroadcastExchange=cyclops.udr.broadcast
  
  # Consumer (RabbitMQ) credentials
  ConsumerHost=localhost
  ConsumerUsername=cyclops
  ConsumerPassword=password
  ConsumerPort=5672
  ConsumerVirtualHost=cyclops
  ConsumerDataQueue=cyclops.udr.consume
  ConsumerCommandsQueue=cyclops.udr.commands

- ServerHTTPPort / ServerHTTPSPort: You can configure the port where the service will be running at. HTTPS is supported if you provide a valid certificate and the associated password.
- TimescaleDB parameters are same as Postgressql parameters
- RabbitMQ block configures how this service communicates with an existing RabbitMQ service endpoint, they are defined for both the consumer as well as publisher process.

Fixing permissions
------------------
Before running any of the Cyclops framework services via *systemctl* command, 
make sure that the process user *cyclops* which was created earlier to run the 
process has full read/write access to Cyclops specific system folder and files.

::

  sudo chown -R cyclops:cyclops /var/log/cyclops/
  sudo chown -R cyclops:cyclops /usr/local/bin/cyclops/
  sudo chown -R cyclops:cyclops /etc/cyclops/
  sudo chown -R cyclops:cyclops /var/lib/cyclops/

Setup as a service
------------------
Create a file called *cyclops-udr.service* in */etc/systemd/system/* 
directory. Add the following content to this file:

::

  [Unit]
  Description=Cyclops UDR Service
  After=network.target rabbitmq-server.service postgresql-9.6.service
  
  [Service]
  ExecStartPre=/bin/sleep 2
  Type=simple
  User=cyclops
  ExecStart=/usr/bin/java -jar /usr/local/bin/cyclops/udr/udr.jar /etc/cyclops/udr/udr.conf
  Restart=on-abort
  
  [Install]
  WantedBy=multi-user.target

This assumes that the rabbitmq and postgres server is running in the same 
machine where you are setting up udr service. If not then remove them from the 
dependencies list by changing the **After** line above. *Do make sure that 
these services are running and reachable before udr service is started*.

You can enable and manage the udr service and start it by using the following 
systemctl commands.

::

  sudo systemctl enable cyclops-udr.service
  sudo systemctl start/stop/restart/status cyclops-udr.service
