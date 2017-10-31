========================
Install & Configure: CDR
========================

Let us setup CDR micro-service to run as a linux system service.

Preparing the host machine
--------------------------
Start by creating system folders for CDR service.

::

  sudo mkdir -p /var/log/cyclops/cdr/
  sudo mkdir -p /etc/cyclops/cdr/
  sudo mkdir -p /usr/local/bin/cyclops/cdr/

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

  sudo touch /var/log/cyclops/cdr/errors.log
  sudo touch /var/log/cyclops/cdr/trace.log
  sudo touch /var/log/cyclops/cdr/rest.log
  sudo touch /var/log/cyclops/cdr/dispatch.log
  sudo touch /var/log/cyclops/cdr/data.log
  sudo touch /var/log/cyclops/cdr/commands.log
  sudo touch /var/log/cyclops/cdr/timeseries.log

Let's move the binary and the configuration files from the compiled locations 
to the target system destinations.

::

  sudo mv CDR/target/cdr.jar /usr/local/bin/cyclops/cdr/
  sudo mv CDR/config/cdr.conf /etc/cyclops/cdr/

Preparing the Postgressql / TimescaleDB
---------------------------------------
Before working with the cdr service, it is necessary to setup the appropriate 
database and table schemas. This can be achieved by executing the following 
commands on the host where the Postgresql service is running.

::

  psql -U postgres -h localhost <<EOF
  CREATE DATABASE cyclops_cdr WITH OWNER cyclops;
  GRANT ALL PRIVILEGES ON DATABASE cyclops_cdr TO cyclops;
  EOF

::

  psql -U cyclops -h localhost -d cyclops_cdr <<EOF
  CREATE TABLE IF NOT EXISTS cdr (
    time_from TIMESTAMP         NOT NULL,
    time_to   TIMESTAMP         NOT NULL,
    metric    TEXT              NOT NULL,
    account   TEXT              NOT NULL,
    charge    DOUBLE PRECISION  NOT NULL,
    data      JSONB,
    currency  TEXT
  );
  CREATE INDEX IF NOT EXISTS cdr_metric ON cdr (metric, time_from DESC);
  CREATE INDEX IF NOT EXISTS cdr_account ON cdr (account, time_from DESC);
  CREATE INDEX IF NOT EXISTS cdr_currency ON cdr (currency, time_from DESC);
  CREATE INDEX IF NOT EXISTS cdr_data ON cdr USING HASH (data);
  EOF

Preparing RabbitMQ
------------------
Assuming that RabbitMQ is running on the same machine where the following 
commands are to be executed, running these will setup necessary exchanges, 
queues and bindings between them for cdr process to function properly.

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.cdr.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.cdr.commands

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.coincdr.broadcast

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.cdr.dispatch

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.cdr.broadcast

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/cyclops/e/cyclops.coincdr.broadcast/q/cyclops.cdr.consume

In the above commands, do not forget to replace the **-u** values *cyclops* 
and *pass1234* to correct RabbitMQ user/pass values that was setup earlier.

Configuring CDR
---------------
You can configure the service endpoints and dependencies in the configuration 
file located under */etc/cyclops/cdr/*

Default content is shown next:
::

  # HTTP and/or HTTPS port to be exposed at
  ServerHTTPPort=4568
  #ServerHTTPSPort=5568
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
  DatabaseName=cyclops_cdr
  DatabasePageLimit=500
  DatabaseConnections=2

  # Publisher (RabbitMQ) credentials
  PublisherHost=localhost
  PublisherUsername=cyclops
  PublisherPassword=password
  PublisherPort=5672
  PublisherVirtualHost=cyclops
  PublisherDispatchExchange=cyclops.cdr.dispatch
  PublisherBroadcastExchange=cyclops.cdr.broadcast

  # Consumer (RabbitMQ) credentials
  ConsumerHost=localhost
  ConsumerUsername=cyclops
  ConsumerPassword=password
  ConsumerPort=5672
  ConsumerVirtualHost=cyclops
  ConsumerDataQueue=cyclops.cdr.consume
  ConsumerCommandsQueue=cyclops.cdr.commands

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
Create a file called *cyclops-cdr.service* in */etc/systemd/system/* 
directory. Add the following content to this file:

::

  [Unit]
  Description=Cyclops CDR Service
  After=network.target rabbitmq-server.service postgresql-9.6.service
  
  [Service]
  ExecStartPre=/bin/sleep 2
  Type=simple
  User=cyclops
  ExecStart=/usr/bin/java -jar /usr/local/bin/cyclops/cdr/cdr.jar /etc/cyclops/cdr/cdr.conf
  Restart=on-abort
  
  [Install]
  WantedBy=multi-user.target

This assumes that the rabbitmq and postgres server is running in the same 
machine where you are setting up cdr service. If not then remove them from the 
dependencies list by changing the **After** line above. *Do make sure that 
these services are running and reachable before cdr service is started*.

You can enable and manage the cdr service and start it by using the following 
systemctl commands.

::

  sudo systemctl enable cyclops-cdr.service
  sudo systemctl start/stop/restart/status cyclops-cdr.service
