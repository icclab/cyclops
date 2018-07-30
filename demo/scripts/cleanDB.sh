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
# Desription: Data base cleaning script for Cyclops demo

# Default help text
HELP_TEXT=$'\nCyclops database cleaning script. Allows to delete usage, udr, cdr and bills from database.
Mandatory options:
-r(--rules) - to delete rules from database;
-d(--data) - to delete usage, udr, cdr and bills from database.
Example:

bash cleanDB.sh -d
bash cleanDB.sh -d -r\n'

# Function to delete all the usage, udr's, cdr's and bills from database
delete_data() {
psql -U username -h localhost -d cyclops_udr <<EOF
delete from udr;
delete from usage;
\q
EOF

psql -U username -h localhost -d cyclops_cdr <<EOF
delete from cdr;
\q
EOF

psql -U username -h localhost -d cyclops_billing <<EOF
delete from bill;
\q
EOF
}

# Function to delete all the rules from database
delte_rules(){
psql -U username -h localhost -d cyclops_cdr <<EOF
delete from instanceorm;
\q
EOF

psql -U username -h localhost -d cyclops_billing <<EOF
delete from instanceorm;
\q
EOF
}

# Read script's parameters
if ! [[ -z "$@" ]]; 
then
	for i in "$@"
		do
			case $i in 
			-r|--rules)
			delte_rules
			shift
			;; 
			-d|--data)
			delete_data
			shift
			;;
			-h|--help)
			echo "$HELP_TEXT"
			exit 1
			shift
			;;
			*)
			echo "$HELP_TEXT"
			exit 1
			shift
			;;
			esac
	done
else
	echo "$HELP_TEXT"
	exit 1
fi