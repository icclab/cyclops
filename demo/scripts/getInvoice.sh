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
# Desription: Invoice genration script for Cyclops demo

# Default help text
HELP_TEXT=$'\nCyclops data generation script with the following mandatory options:
-t0(--timestart)= starting timestamp;
-t1(--timestop)= ending timestamp.
-d(--delay)= deleay between commands in SECONDS.

Example:

bash getInvoice.sh -t0=21-Sep-2017 -t1=23-Sep-2017 -d=2
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
		-d=*|--delay=*)
		DELAY="${i#*=}"
		shift
		;;
	esac
done

# Parameters initialization check 
if [[ -z "$TIMESTART" || -z "$TIMESTOP" || -z "$DELAY" ]];
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

# Check if the delay is integer
re='^[0-9]+$'
if ! [[ $DELAY =~ $re ]] ; 
	then
    error_exit "error: Delay option is not a number! Aborting."
fi

echo "Input data in millisec: [time0 = $time0; time1 = $time1; delay = $DELAY]" 

# Input dates correctness check
if [ "${time1}" -lt "${time0}" ]
	then
	echo "Starting time must not be biger then ending time! Aborting."
	exit 1
fi

echo
# Generating udr
json1='{
  "command": "GenerateUDRs",
  "time_from": '$time0',
  "time_to": '$time1'
}'
curl -X POST http://localhost:4567/command -H "content-type: application/json" -d "${json1}"

sleep $DELAY

echo 
# Flush udr's
json2='{
  "command": "FlushUDRs",
  "time_from": '$time0',
  "time_to": '$time1'
}'
curl -X POST http://localhost:4567/command -H "content-type: application/json" -d "${json2}"

sleep $DELAY

echo
# Generate the bill
json3='{
  "command": "GenerateBill",
  "time_from": '$time0',
  "time_to": '$time1',
  "request": "dord"
}'
curl -X POST http://localhost:4569/command -H "content-type: application/json" -d "${json3}"

echo
echo "Generated bill as JSON:"
curl -X GET http://localhost:4569/bill -H "content-type: application/json" 
