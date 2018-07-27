
######################################
############ Cyclops USER ############
######################################
psql <<- EOSQL
	CREATE USER username WITH PASSWORD 'password';
EOSQL
######################################
############ Cyclops DBs #############
######################################
psql <<- EOSQL
	CREATE DATABASE cyclops_udr WITH OWNER username;
	CREATE DATABASE cyclops_cdr WITH OWNER username;
	CREATE DATABASE cyclops_billing WITH OWNER username;
	GRANT ALL PRIVILEGES ON DATABASE cyclops_udr TO username;
	GRANT ALL PRIVILEGES ON DATABASE cyclops_cdr TO username;
	GRANT ALL PRIVILEGES ON DATABASE cyclops_billing TO username;
EOSQL
######################################
############ Cyclops UDR #############
######################################
psql -d cyclops_udr <<- EOSQL
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
EOSQL

psql -d cyclops_udr <<- EOSQL
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
EOSQL
######################################
############ Cyclops CDR #############
######################################
psql -d cyclops_cdr <<- EOSQL
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
EOSQL
######################################
########## Cyclops Billing ###########
######################################
psql -d cyclops_billing<<- EOSQL
CREATE TABLE IF NOT EXISTS billrun (
  id        SERIAL            primary key,
  time      TIMESTAMP         NOT NULL,
  data      JSONB
);

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
EOSQL

