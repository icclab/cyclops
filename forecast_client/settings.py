"""
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 """
import configparser


class Settings:
    def __init__(self, path):
        config = configparser.ConfigParser()
        config.read(path)
        self.cdrRuleEndpoint = config['ENDPOINTS']['cdrRuleEndpoint']
        self.billRuleEndpoint = config['ENDPOINTS']['billRuleEndpoint']
        self.forecastEndpoint = config['ENDPOINTS']['udrEndpoint']
        self.dbuser = config['DB']['user']
        self.dbpass = config['DB']['password']
        self.dbhost = config['DB']['host']
        self.dbport = config['DB']['port']
        self.udrdb = config['DB']['cyclops_udr']
        self.cdrdb = config['DB']['cyclops_cdr']
        self.billdb = config['DB']['cyclops_billing']
