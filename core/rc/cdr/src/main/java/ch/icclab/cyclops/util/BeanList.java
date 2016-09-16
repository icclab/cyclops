package ch.icclab.cyclops.util;
/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Skoviera
 * Created: 09/08/16
 * Description: Utility to populate list of beans
 */
public class BeanList {
    public static List populate(List<Map> list, Class clazz) {
        List<Object> mapped = new ArrayList<>();

        // iterate and map those objects
        if (list != null) {
            for (Map map : list) {
                try {
                    Object bean = clazz.newInstance();

                    // map HashMap to POJO
                    BeanUtils.populate(bean, map);

                    // add it to list of mapped CDRs
                    mapped.add(bean);

                } catch (Exception ignored) {
                    return null;
                }
            }
        }

        return mapped;
    }
}
