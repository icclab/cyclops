package ch.icclab.cyclops.util;
/*
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
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 19/02/16
 * Description: Parse Template fields
 */
public class RegexParser {

    /**
     * Parse Rule template and get available fields
     * @param template content
     * @return list of fields
     */
    public static List<String> getFieldsFromTemplate(String template) {
        List<String> list = new ArrayList<String>();

        // regex we are going to be using
        String regex = "^template[ ]*header(.+?)template";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(template);

        // in case something is there
        if (matcher.find()) {
            // extract regex from it
            String content = matcher.group(1);

            String extract = "^\\s*([\\S]*)\\s*$";
            Pattern patt = Pattern.compile(extract, Pattern.MULTILINE);
            Matcher match = patt.matcher(content);

            // add to the list
            while (match.find()) {
                String str = match.group(1);

                // only add it's not empty
                if (!str.isEmpty()) {
                    list.add(str);
                }
            }
        }

        return list;
    }

    /**
     * Extract name from provided template
     * @param template content
     * @return name
     */
    public static String getNameFromTemplate(String template) {
        String regex = "template\\s*\"(.*)\"";
        return executeRegex(template, regex);
    }

    /**
     * Extract name from provided rule
     * @param content of the rule
     * @return name
     */
    public static String getNameFromRule(String content) {
        String regex = "rule\\s*\"(.*)\"";
        return executeRegex(content, regex);
    }

    /**
     * Use specified regex on provided content
     * @param content to be parsed
     * @param regex to be used
     * @return found string
     */
    private static String executeRegex(String content, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        // return name
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    /**
     * Get filename from provided path
     * @param path string
     * @return file name
     */
    public static String getFileName(String path) {
        String regex = "(\\w+).class";
        return executeRegex(path, regex);
    }
}
