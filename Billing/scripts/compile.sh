#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Compiling Billing"
cd ..

source /etc/environment
export JAVA_HOME="/usr/lib/jvm/java-8-oracle"

mvn dependency:tree
mvn package assembly:single

mv target/cyclops-billing-3.0.0-jar-with-dependencies.jar billing.jar

