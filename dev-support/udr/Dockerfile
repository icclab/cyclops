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
# Author: Piyush Harsh,
# URL: piyush-harsh.info
#
# Thanks to: https://hub.docker.com/_/java/

FROM cyclopsbilling/cyclops-base
EXPOSE 4567

RUN apt-get update
RUN apt-get install -y curl

RUN wget https://raw.githubusercontent.com/icclab/cyclops-binaries/master/bin/udr.jar
ADD udr.conf udr.conf

ADD test-rabbit.sh run.sh
RUN chmod +x run.sh

CMD ["/bin/bash", "run.sh"]

# CMD ["java", "-jar", "udr.jar", "udr.conf"]

