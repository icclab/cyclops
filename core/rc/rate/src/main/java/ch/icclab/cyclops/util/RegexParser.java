/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

package ch.icclab.cyclops.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Skoviera
 * Created: 19/02/16
 * Description: Parse fields
 */
public class RegexParser {

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
