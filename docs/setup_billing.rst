============================
Install & Configure: Billing
============================

Let us setup Billing micro-service to run as a linux system service.

Preparing the host machine
--------------------------
Start by creating system folders for Billing service.

::

  sudo mkdir -p /var/log/cyclops/billing/
  sudo mkdir -p /etc/cyclops/billing/
  sudo mkdir -p /usr/local/bin/cyclops/billing/

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

  sudo touch /var/log/cyclops/billing/errors.log
  sudo touch /var/log/cyclops/billing/trace.log
  sudo touch /var/log/cyclops/billing/rest.log
  sudo touch /var/log/cyclops/billing/dispatch.log
  sudo touch /var/log/cyclops/billing/data.log
  sudo touch /var/log/cyclops/billing/commands.log
  sudo touch /var/log/cyclops/billing/timeseries.log

Let's move the binary and the configuration files from the compiled locations 
to the target system destinations.

::

  sudo mv Billing/target/billing.jar /usr/local/bin/cyclops/billing/
  sudo mv Billing/config/billing.conf /etc/cyclops/billing/

Preparing the Postgressql / TimescaleDB
---------------------------------------
Before working with the billing service, it is necessary to setup the 
appropriate database and table schemas. This can be achieved by executing the 
following commands on the host where the Postgresql service is running.

::

  psql -U postgres -h localhost <<EOF
  CREATE DATABASE cyclops_billing WITH OWNER cyclops;
  GRANT ALL PRIVILEGES ON DATABASE cyclops_billing TO cyclops;
  EOF

::

  psql -U cyclops -h localhost -d cyclops_billing <<EOF
  CREATE TABLE IF NOT EXISTS billrun (
    id        SERIAL            primary key,
    time      TIMESTAMP         NOT NULL,
    data      JSONB
  );
  EOF

::
  psql -U cyclops -h localhost -d cyclops_billing <<EOF
  CREATE TABLE IF NOT EXISTS bill (
    id        SERIAL,
    run       INTEGER           REFERENCES billrun,
    time_from TIMESTAMP         NOT NULL,
    time_to   TIMESTAMP         NOT NULL,
    account   TEXT              NOT NULL,
    charge    DOUBLE PRECISION  NOT NULL,
    discount  TEXT,
    data      JSONB,
    currency  TEXT
  );
  CREATE INDEX IF NOT EXISTS bill_account ON bill (account, time_from DESC);
  CREATE INDEX IF NOT EXISTS bill_currency ON bill (currency, time_from DESC);
  CREATE INDEX IF NOT EXISTS bill_data ON bill USING HASH (data);
  EOF

Preparing RabbitMQ
------------------
Assuming that RabbitMQ is running on the same machine where the following 
commands are to be executed, running these will setup necessary exchanges, 
queues and bindings between them for billing process to function properly.

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.billing.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.billing.commands

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.coinbill.broadcast

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.billing.dispatch

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/cyclops/cyclops.billing.broadcast

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/cyclops/e/cyclops.coinbill.broadcast/q/cyclops.billing.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.cdr.commands

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/cyclops/cyclops.coinbill.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"CDR\"}" http://localhost:15672/api/bindings/cyclops/e/cyclops.billing.dispatch/q/cyclops.cdr.commands

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"CoinBill\"}" http://localhost:15672/api/bindings/cyclops/e/cyclops.billing.dispatch/q/cyclops.coinbill.consume

::

  curl -u "cyclops:pass1234" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"SelfPublish\"}" http://localhost:15672/api/bindings/cyclops/e/cyclops.billing.dispatch/q/cyclops.billing.commands

In the above commands, do not forget to replace the **-u** values *cyclops* 
and *pass1234* to correct RabbitMQ user/pass values that was setup earlier.

Configuring Billing
---------------
You can configure the service endpoints and dependencies in the configuration 
file located under */etc/cyclops/billing/*

Default content is shown next:
::

  # HTTP and/or HTTPS port to be exposed at
  ServerHTTPPort=4569
  #ServerHTTPSPort=5569
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
  DatabaseName=cyclops_billing
  DatabasePageLimit=500
  DatabaseConnections=2

  # Publisher (RabbitMQ) credentials
  PublisherHost=localhost
  PublisherUsername=cyclops
  PublisherPassword=password
  PublisherPort=5672
  PublisherVirtualHost=cyclops
  PublisherDispatchExchange=cyclops.billing.dispatch
  PublisherBroadcastExchange=cyclops.billing.broadcast

  # Consumer (RabbitMQ) credentials
  ConsumerHost=localhost
  ConsumerUsername=cyclops
  ConsumerPassword=password
  ConsumerPort=5672
  ConsumerVirtualHost=cyclops
  ConsumerDataQueue=cyclops.billing.consume
  ConsumerCommandsQueue=cyclops.billing.commands

  # Bill generation workflow
  PublishToCDRWithKey=CDR
  PublishToCoinBillWithKey=CoinBill
  PublishToSelf=SelfPublish

  # Connection to customer-database
  CustomerDatabaseHost=localhost
  CustomerDatabasePort=8888

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
Create a file called *cyclops-billing.service* in */etc/systemd/system/* 
directory. Add the following content to this file:

::

  [Unit]
  Description=Cyclops billing Service
  After=network.target rabbitmq-server.service postgresql-9.6.service
  
  [Service]
  ExecStartPre=/bin/sleep 2
  Type=simple
  User=cyclops
  ExecStart=/usr/bin/java -jar /usr/local/bin/cyclops/billing/billing.jar /etc/cyclops/billing/billing.conf
  Restart=on-abort
  
  [Install]
  WantedBy=multi-user.target

This assumes that the rabbitmq and postgres server is running in the same 
machine where you are setting up billing service. If not then remove them from 
the dependencies list by changing the **After** line above. *Do make sure that 
these services are running and reachable before billing service is started*.

You can enable and manage the billing service and start it by using the 
following systemctl commands.

::

  sudo systemctl enable cyclops-billing.service
  sudo systemctl start/stop/restart/status cyclops-billing.service
