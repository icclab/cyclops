#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/coin/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/coin/errors.log
sudo touch /var/log/cyclops/coin/trace.log
sudo touch /var/log/cyclops/coin/hibernate.log
sudo touch /var/log/cyclops/coin/facts.log
sudo touch /var/log/cyclops/coin/rules.log
sudo touch /var/log/cyclops/coin/timeline.log
sudo touch /var/log/cyclops/coin/dispatch.log
sudo touch /var/log/cyclops/coin/stream.log

find /var/log/cyclops/coin -type f -exec sudo chmod 666 {} \;

sudo -k