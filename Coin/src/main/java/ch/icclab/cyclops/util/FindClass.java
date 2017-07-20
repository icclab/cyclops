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

import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 07/09/16
 * Description: Recursively find class in a package
 */
public class FindClass {

    /**
     * Look into package and recursively find a class
     * @param pcg to look into
     * @param clazz to find
     * @return Class or null
     */
    public static Class inPackage(Package pcg, String clazz){
        try {
            List<Class> list = new ArrayList<>();

            list.addAll(new ClassesInPackageScanner().setResourceNameFilter((packageName, fileName) -> clazz.equalsIgnoreCase(RegexParser.getFileName(fileName))).scan(pcg.getName()));

            return Class.forName(list.get(0).getName());
        } catch (Exception e) {
            return null;
        }
    }
}
