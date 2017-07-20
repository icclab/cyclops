#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/billing/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/billing/errors.log
sudo touch /var/log/cyclops/billing/trace.log
sudo touch /var/log/cyclops/billing/timeseries.log
sudo touch /var/log/cyclops/billing/rest.log
sudo touch /var/log/cyclops/billing/dispatch.log
sudo touch /var/log/cyclops/billing/data.log
sudo touch /var/log/cyclops/billing/commands.log

find /var/log/cyclops/billing -type f -exec sudo chmod 666 {} \;

sudo -k