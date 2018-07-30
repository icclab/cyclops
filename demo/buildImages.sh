#!/bin/bash

# Copyright (c) 2018. SPLab, Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.

# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at

# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

# Author: Dorodko Serhii (dord@zhaw.ch)
# Created: 27.07.2018
# Desription: Script to building docker images for Cyclops demo

# Array of docker context directories 
declare -a srcImages=("cyclops_udr" 
					  "cyclops_cdr" 
					  "cyclops_billing" 
					  "cyclops_coin_cdr" 
					  "cyclops_coin_bill" 
					  "cyclops_rabbitmq_init"
					  "cyclops_rabbitmq"
					  "cyclops_timescaledb")

for i in "${srcImages[@]}"
do
	docker build -t "$i" "$i" --no-cache
done

# Remove <none> images after build
docker rmi -f $(docker images --filter "dangling=true" -q --no-trunc)
 

