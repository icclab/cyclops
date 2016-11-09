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

### Installing InfluxDB database
wget https://dl.influxdata.com/influxdb/releases/influxdb_1.0.2_amd64.deb
sudo dpkg -i influxdb_1.0.2_amd64.deb
sudo /etc/init.d/influxdb restart

### Installing InfluxDB Java client
sudo apt-get install git
git clone https://github.com/influxdb/influxdb-java.git
cd influxdb-java
rm -fR src/test
mvn clean install -DskipTests=true
cd ..
rm -fR influxdb-java