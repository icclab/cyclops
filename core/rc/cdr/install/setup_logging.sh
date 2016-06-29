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
sudo mkdir -p /var/log/cyclops/rc/cdr/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/rc/cdr/errors.log
sudo touch /var/log/cyclops/rc/cdr/trace.log
sudo touch /var/log/cyclops/rc/cdr/timeseries.log
sudo touch /var/log/cyclops/rc/cdr/rest.log
sudo touch /var/log/cyclops/rc/cdr/dispatch.log
sudo touch /var/log/cyclops/rc/cdr/data.log
sudo touch /var/log/cyclops/rc/cdr/commands.log

find /var/log/cyclops/rc/cdr -type f -exec sudo chmod 666 {} \;

sudo -k