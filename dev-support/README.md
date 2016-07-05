## Quick deployment instructions with Docker

Info: the images used in the provided *docker-compose.yml* script are from Docker hub registry: **cyclopsbilling**.

### Prerequisites
You must have *docker* and *docker-compose* installed in your dev-environment.

### Launching Cyclops
For quick tests, the provided configuration files are adequate, if you wish to change the ports of other services, please make sure that you reflect those changes in various configuration files appropriately.

After verifying the configuration files in each sub-folder for correctness, simply execute the command below.

```
docker-compose up
```
#### Pre-test setting
In order to setup queue bindings properly, a few scripts are included in the *scripts* subfolder. Execute them as follows -

```
chmod +x *.sh
./bindings_rcb_rate.sh localhost 15672
./bindings_cloudstack.sh localhost 15672
```
Change *localhost* to the url where rabbitmq is running in case you are running the containers remotely. Now simply follow the developers and user's guide available in the WiKi to test or extend Cyclops, or to write your own collectors.

### Not included in this release
Our generic rule-engine *Coin* is not included in this release, but will be released once the legal release restrictions expire in next few months. Dashboard is also not included, and once we have prepared the docker container for the basic dashboard it will be incuded.
 
### Documentation
Comprehensive API reference and developer's guides, as well as troubleshooting pages are available in the Wiki section.

### Communication
  * Email: icclab-rcb-cyclops[at]dornbirn[dot]zhaw[dot]ch
  * Website: <a href="http://icclab.github.io/cyclops" target="_blank">icclab.github.io/cyclops</a>
  * Blog: <a href="http://blog.zhaw.ch/icclab" target="_blank">http://blog.zhaw.ch/icclab</a>
  * Tweet us @<a href="https://twitter.com/rcb_cyclops" target="_blank">RCB_Cyclops</a>
   
### Developed @
<img src="https://blog.zhaw.ch/icclab/files/2016/03/cropped-service_engineering_logo_zhawblue_banner.jpg" alt="ICC Lab" height="180" width="620"></img>

### License
 
      Licensed under the Apache License, Version 2.0 (the "License"); you may
      not use this file except in compliance with the License. You may obtain
      a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations
      under the License.