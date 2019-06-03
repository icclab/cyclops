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
# Desription: Data generation script for Cyclops demo


# Random int number from 0 to 2048
generateMemoryUsage() {
	echo "$((RANDOM % 2048))"
}

# Usage JSON collected from k8s cluster generating function 
#
# Example: 
# {
# 	"metric": "memory",
# 	"account": "dord",
# 	"time": 1493450936000,
# 	"usage": 22560,
# 	"unit": "KB",
# 	"data":{
# 		"pod_id":"string id",
# 		}
# 	}   
# }
genarateUsageQuery () {
	echo '{
	"metric": "memory",
	"account": "'$1'",
	"time": '$2',
	"usage": '$3',
	"unit": "MB",
	"data":{
		"pod_id":"'$4'"
		}
	}'
}

# Sends json to usage endpoint of Cyclops. Paramete as json to send
sendUsage () {
	local json=$1 
	curl -X "POST" "http://localhost:4567/usage" -H "Content-Type: application/json" -d "${json}"
}

# Default help text
HELP_TEXT=$'\nCyclops data generation script with the following mandatory options:
-t0(--timestart)= starting timestamp;
-t1(--timestop)= ending timestamp;
-i(--interval)= time window in MINUTES for generation.

Example:

bash dataGeneration.sh -t0=21-Sep-2017 -t1=23-Sep-2017 -i=360
'

error_exit()
{
	echo "$1" 1>&2
	echo "$HELP_TEXT"
	exit 1
}

# Read script parameters 
for i in "$@"
do
	case $i in 
		-h|--help)
		echo "$HELP_TEXT"
		exit 1
		shift
		;;
		-t0=*|--timestart=*)
		TIMESTART="${i#*=}"
		shift
		;;
		-t1=*|--timestop=*)
		TIMESTOP="${i#*=}"
		shift
		;;
		-i=*|--interval=*)
		INTERVAL="${i#*=}"
		shift
		;;
	esac
done

# parameter initialization check
if [[ -z "$TIMESTART" || -z "$TIMESTOP" || -z "$INTERVAL" ]];
	then
	error_exit "error: Parameters are not set. Aborting."
fi

# Converting input data to milliseconds
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     
	time0=$(date -d "$TIMESTART" '+%s%3N')
	time1=$(date -d "$TIMESTOP" '+%s%3N')
	if [ $? -ne 0 ];
		then
		error_exit "error: Input time has incorrect format! Aborting."	
	fi
	;;
    Darwin*)    
	time0=$(date -j -f "%a %b %d %T %Z %Y" "`date`" "+%s%3N")
	time1=$(date -j -f "%a %b %d %T %Z %Y" "`date`" "+%s%3N")
	if [ $? -ne 0 ]; 
		then
		error_exit "error: Input time has incorrect format! Aborting."
	fi
	;;
    *)          
	echo "Current system is not supported. Suported systems: MacOS, Linux. Aborting."
	exit 1
esac

# Check if the interval is integer
re='^[0-9]+$'
if ! [[ $INTERVAL =~ $re ]] ; 
	then
    error_exit "error: Interval option is not a number! Aborting."
fi

window=$((${INTERVAL} * 60000))
 


# Input dates correctness check
if [ "${time1}" -lt "${time0}" ]
	then
	echo "Starting time must not be biger then ending time! Aborting."
	exit 1
fi

timestamp=${time0}
COUNT=0
# Deploy usage data to Cyclops through API call in loop
echo "Deploying manufacturing data to Cyclops..."
while [ $timestamp -le "$time1" ]
do
  echo
  json=$(genarateUsageQuery "dord" "$timestamp" "$(generateMemoryUsage)" "pod1")
  echo $'Sending the following usage:\n'
  echo "$json"
  echo
  sendUsage "$json" 
  echo 
  timestamp=$(($timestamp+$window))
  COUNT=$((${COUNT} + 1))
done

echo
echo "Input data in millisec: [time0 = $time0; time1 = $time1; window = $window]"
echo
echo "records generated and fed #$COUNT"


