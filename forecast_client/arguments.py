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
import argparse


class Arguments:
    def __init__(self):
        parser = argparse.ArgumentParser()
        subparsers = parser.add_subparsers(help="Available commands", dest='command')

        parser_rule = subparsers.add_parser('rule', help="Rule management")
        parser_rule.add_argument('--delete', action="store_true", help="Delete rule")
        parser_rule.add_argument('--engine', required=True, help="The coin instance to use", choices=['cdr', 'bill'])
        parser_rule.add_argument('--target', required=True, help="Rule-file path to add, or name to delete")

        parser_clean = subparsers.add_parser('cleanup', help="Clean up the database")
        parser_clean.add_argument("--target", required=True, help="Pricing model to clean up")
        parser_clean.add_argument("--all", action="store_true", help="Delete the model's rules too")

        parser_forecast = subparsers.add_parser('forecast', help="Generate forecast")
        forecasts = parser_forecast.add_subparsers(help="Available forecasts", dest='forecast')

        singe_forecast = forecasts.add_parser('single', help="Forecast for single account")
        singe_forecast.add_argument('--account', required=True, help="Target account")
        singe_forecast.add_argument('--model', required=True, help="Pricing model to be used")
        singe_forecast.add_argument('--length', type=int, default=30, help="Number of days to forecast")

        global_forecast = forecasts.add_parser('global', help="Global forecast")
        global_forecast.add_argument('--model', required=True, help="Pricing model to be used")
        global_forecast.add_argument('--length', type=int, default=30, help="Number of days to forecast")

        pattern_forecast = forecasts.add_parser('pattern', help="Pattern based global forecast")
        pattern_forecast.add_argument('--model', required=True, help="Pricing model to be used")
        pattern_forecast.add_argument('--length', type=int, default=30, help="Number of days to forecast")

        self.args = parser.parse_args()
