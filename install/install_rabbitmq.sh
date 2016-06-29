#!/bin/bash
# Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

sudo apt-get -y install rabbitmq-server
sudo update-rc.d rabbitmq-server defaults
sudo /etc/init.d/rabbitmq-server restart
sudo rabbitmq-plugins enable rabbitmq_management
sudo /etc/init.d/rabbitmq-server restart
wget http://localhost:15672/cli/rabbitmqadmin
chmod +x rabbitmqadmin
sudo mv rabbitmqadmin /usr/bin/