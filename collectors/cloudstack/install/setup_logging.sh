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
sudo mkdir -p /var/log/cyclops/cloudstack/
find /var/log/cyclops -type d -exec sudo chmod 755 {} \;

sudo touch /var/log/cyclops/cloudstack/errors.log
sudo touch /var/log/cyclops/cloudstack/trace.log
sudo touch /var/log/cyclops/cloudstack/scheduler.log
sudo touch /var/log/cyclops/cloudstack/dispatch.log
sudo touch /var/log/cyclops/cloudstack/hibernate.log

find /var/log/cyclops/cloudstack -type f -exec sudo chmod 666 {} \;

sudo -k