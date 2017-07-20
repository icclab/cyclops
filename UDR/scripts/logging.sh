#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/udr/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/udr/errors.log
sudo touch /var/log/cyclops/udr/trace.log
sudo touch /var/log/cyclops/udr/timeseries.log
sudo touch /var/log/cyclops/udr/rest.log
sudo touch /var/log/cyclops/udr/dispatch.log
sudo touch /var/log/cyclops/udr/data.log
sudo touch /var/log/cyclops/udr/commands.log

find /var/log/cyclops/udr -type f -exec sudo chmod 666 {} \;

sudo -k