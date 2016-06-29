#!/bin/bash
# Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# Author: Piyush Harsh, Martin Skoviera

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