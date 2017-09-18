#!/bin/bash
# Copyright (c) 2017. Cyclops Labs GmbH
# All Rights Reserved.
#
# Author: Martin Skoviera  linkedin.com/in/skoviera

echo "Compiling Usage Generator"
cd ..

source /etc/environment
export JAVA_HOME="/usr/lib/jvm/java-8-oracle"

mvn dependency:tree
mvn package assembly:single

mv target/cyclops-usage-generator-2.3.0-jar-with-dependencies.jar bin/cyclops_usage_generator.jar

