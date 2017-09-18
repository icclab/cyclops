#!/bin/bash
# Copyright (c) 2017. Cyclops Labs GmbH
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/usage_generator/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/usage_generator/errors.log
sudo touch /var/log/cyclops/usage_generator/trace.log
sudo touch /var/log/cyclops/usage_generator/timeseries.log
sudo touch /var/log/cyclops/usage_generator/rest.log
sudo touch /var/log/cyclops/usage_generator/dispatch.log
sudo touch /var/log/cyclops/usage_generator/data.log
sudo touch /var/log/cyclops/usage_generator/commands.log

find /var/log/cyclops/usage_generator -type f -exec sudo chmod 666 {} \;

sudo -k