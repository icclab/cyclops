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
# Author: Martin Skoviera

echo "Stopping any previous deployments"
echo ""
bash stop_cyclops.sh 1>/dev/null 2>&1

printf "Starting the RCB Cyclops "
printf "UDR ... "
nohup java -jar core/udr/bin/udr.jar core/udr/config/udr.conf > nohup.out &
echo $! >> pid.out

printf "Static rating ... "
nohup java -jar core/rc/rate/bin/rate.jar core/rc/rate/config/rate.conf > nohup.out &
echo $! >> pid.out

printf "CDR ... "
nohup java -jar core/rc/cdr/bin/cdr.jar core/rc/cdr/config/cdr.conf > nohup.out &
echo $! >> pid.out

printf "Billing ... "
nohup java -jar core/billing/bin/billing.jar core/billing/config/billing.conf > nohup.out &
echo $! >> pid.out

echo ""
echo ""
echo "All micro services started and are running in the background."
echo ""
echo "Console outputs are in nohup.out"
echo ""
echo "Server logs are in /var/log/cyclops"
echo ""
echo "To terminate micro services run \"stop_cyclops.sh\""