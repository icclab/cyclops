/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.util

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Author: Skoviera
 * Created: 19/02/16
 * Description: Spock test for RegexParser - data driven test
 */
class RegexParserTest extends Specification {
    @Unroll
    def "Extract available fields from Rule Template"() {
        expect:
        RegexParser.getFieldsFromTemplate(template) == list

        where:
        template            | list
        templateExample     | ["greaterThan", "lessThan", "rate"]
    }

    @Unroll
    def "Extract name from Rule Template"() {
        expect:
        name == RegexParser.getNameFromTemplate(template)

        where:
        name                | template
        "template_name"     | "template \"template_name\""
        "threshold example" | templateExample
    }

    @Unroll
    def "Extract name from Rule"() {
        expect:
        name == RegexParser.getNameFromRule(rule)

        where:
        name                | rule
        "rule_name"         | "rule \"rule_name\""
        "Threshold between @{greaterThan} and @{lessThan} (rule_@{row.rowNumber})" | templateExample
    }

    @Unroll
    def "Extract file names from path"() {
        expect:
        fileName == RegexParser.getFileName(path)

        where:
        fileName            | path
        "GenericBillRequest"| "ch/icclab/cyclops/facts/model/topology/GenericBillRequest.class"
        "PersistedFacts"    | "ch/icclab/cyclops/facts/PersistedFacts.class"
        "UDR"               | "ch/icclab/cyclops/facts/model/UDR.class"
        "CDR"               | "CDR.class"
    }

    @Shared templateExample = '''template header

            greaterThan
            lessThan
            rate

            template "threshold example"

            rule "Threshold between @{greaterThan} and @{lessThan} (rule_@{row.rowNumber})"
            when
                $udr: UDR(usage > @{greaterThan}, usage < @{lessThan})
            then
                $udr.setRate(@{rate})
            end

            end template'''
}