#!/bin/bash

######################################
############ Cyclops USER ############
######################################
psql -U postgres -h localhost <<EOF
CREATE USER cyclops WITH PASSWORD 'cyclops';
EOF

######################################
############ Cyclops DBs #############
######################################
psql -U postgres -h localhost <<EOF
CREATE DATABASE cyclops_event_collector WITH OWNER cyclops;
GRANT ALL PRIVILEGES ON DATABASE cyclops_event_collector TO cyclops;
EOF

######################################
#### Cyclops Openstack Collector #####
######################################
psql -U cyclops -h localhost -d cyclops_event_collector <<EOF
CREATE TABLE IF NOT EXISTS nova_event (
  time            TIMESTAMP         NOT NULL,
  account         TEXT              NOT NULL,
  source          TEXT              NOT NULL,
  type            TEXT		    NOT NULL,
  region          TEXT		    NOT NULL,
  processed       BOOLEAN	    NOT NULL,
  memory          DOUBLE PRECISION  NOT NULL,
  vcpus           DOUBLE PRECISION  NOT NULL,
  source_name     TEXT		    NOT NULL,
  disk            DOUBLE PRECISION  NOT NULL,
  ephemeral       DOUBLE PRECISION  NOT NULL,
  flavor          TEXT	 	    NOT NULL,
  number_volumes  INTEGER	    NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS nova_unique ON nova_event (time, source);
CREATE INDEX IF NOT EXISTS nova_processed ON nova_event (source, processed ASC);
CREATE INDEX IF NOT EXISTS nova_time ON nova_event (source, time DESC);
EOF

psql -U cyclops -h localhost -d cyclops_event_collector <<EOF
CREATE TABLE IF NOT EXISTS cinder_event (
  time           TIMESTAMP         NOT NULL,
  account        TEXT              NOT NULL,
  source         TEXT              NOT NULL,
  type           TEXT		   NOT NULL,
  region         TEXT		   NOT NULL,
  processed      BOOLEAN	   NOT NULL,
  disk           DOUBLE PRECISION  NOT NULL,
  volume_name    TEXT		   NOT NULL,
  instance_id    TEXT	 	   
);
CREATE UNIQUE INDEX IF NOT EXISTS cinder_unique ON cinder_event (time, source);
CREATE INDEX IF NOT EXISTS cinder_processed ON cinder_event (source, processed ASC);
CREATE INDEX IF NOT EXISTS cinder_time ON cinder_event (source, time DESC);
EOF

psql -U cyclops -h localhost -d cyclops_event_collector <<EOF
CREATE TABLE IF NOT EXISTS neutron_event (
  time           TIMESTAMP         NOT NULL,
  account        TEXT              NOT NULL,
  source         TEXT              NOT NULL,
  type           TEXT		   NOT NULL,
  region         TEXT		   NOT NULL,
  processed      BOOLEAN	   NOT NULL,
  ip_address     TEXT	 	   NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS neutron_unique ON neutron_event (time, source);
CREATE INDEX IF NOT EXISTS neutron_processed ON neutron_event (source, processed ASC);
CREATE INDEX IF NOT EXISTS neutron_time ON neutron_event (source, time DESC);
EOF






