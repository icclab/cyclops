#!/bin/bash
# Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Compiling UDR"
cd ..

source /etc/environment
export JAVA_HOME="/usr/lib/jvm/java-8-oracle"

mvn dependency:tree
mvn package assembly:single

mv target/cyclops-udr-3.0.0-jar-with-dependencies.jar udr.jar

