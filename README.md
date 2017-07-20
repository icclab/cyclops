<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

# Cyclops: Rating, Charging and Billing framework
Cyclops is a comprehensive dynamic rating-charging and billing solution for cloud services and beyond.

<a href="https://raw.githubusercontent.com/icclab/cyclops/gh-pages/_site/assets/images/architecture/v3.png" target="_blank"><img src="https://raw.githubusercontent.com/icclab/cyclops/gh-pages/_site/assets/images/architecture/v3.png" alt="Architecture" width="620"></img></a>

### Core components
The RCB Cyclops framework is a collection of these core micro services:

  - UDR micro service - metering and usage collection
  - CDR micro service - pricing and charge generation
  - Billing micro service - invoicing and discounting

### Usage collectors
The current release includes the following usage collectors (with many more to come)
  
  - Docker
  - CloudStack
  - OpenStack Events
  - OpenStack Ceilometer
  
### Rule engines
Both CDR and Billing services include flexible rule engine, offering various pricing strategies and supporting hierarchical organisations.

## Deployment
All micro services are written in Java and are embedded so you can easily deploy them in containers or run them locally. Read more in the Wiki.

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
