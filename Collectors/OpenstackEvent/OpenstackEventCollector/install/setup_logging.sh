#!/bin/bash
# Copyright (c) 2017. Cyclops Labs GmbH
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/event_collector/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/event_collector/errors.log
sudo touch /var/log/cyclops/event_collector/trace.log
sudo touch /var/log/cyclops/event_collector/timeseries.log
sudo touch /var/log/cyclops/event_collector/rest.log
sudo touch /var/log/cyclops/event_collector/dispatch.log
sudo touch /var/log/cyclops/event_collector/data.log
sudo touch /var/log/cyclops/event_collector/commands.log

find /var/log/cyclops/event_collector -type f -exec sudo chmod 666 {} \;

sudo -k
