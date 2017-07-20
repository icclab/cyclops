#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/cdr/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/cdr/errors.log
sudo touch /var/log/cyclops/cdr/trace.log
sudo touch /var/log/cyclops/cdr/timeseries.log
sudo touch /var/log/cyclops/cdr/rest.log
sudo touch /var/log/cyclops/cdr/dispatch.log
sudo touch /var/log/cyclops/cdr/data.log
sudo touch /var/log/cyclops/cdr/commands.log

find /var/log/cyclops/cdr -type f -exec sudo chmod 666 {} \;

sudo -k