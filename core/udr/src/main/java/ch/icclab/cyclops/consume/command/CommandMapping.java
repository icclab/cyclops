package ch.icclab.cyclops.consume.command;
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

import ch.icclab.cyclops.util.RegexParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;
import io.gsonfire.GsonFireBuilder;
import io.gsonfire.TypeSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 07/03/16
 * Description: Automatic GSON mapping for Event messages
 */
public class CommandMapping {
    // logger
    final static Logger logger = LogManager.getLogger(CommandMapping.class.getName());

    private static final Gson gson = new GsonFireBuilder()
            .registerTypeSelector(Command.class, new TypeSelector<Command>() {
                @Override
                public Class<? extends Command> getClassForElement(JsonElement jsonElement) {
                    try {

                        String clazz = jsonElement.getAsJsonObject().get(Command.FIELD_FOR_MAPPING).getAsString();

                        // recursively find correct classes
                        List<Class> list = new ArrayList<>();
                        list.addAll(new ClassesInPackageScanner().setResourceNameFilter((packageName, fileName)
                                -> clazz.equals(RegexParser.getFileName(fileName))).scan(CommandMapping.class.getPackage().getName()));

                        // and use the first one
                        return (Class<? extends Command>) Class.forName(list.get(0).getName());
                    } catch (Exception e) {
                        return null;
                    }
                }
            }).createGson();

    /**
     * Map object to provided class
     * @param json string
     * @return mapped object or null
     */
    public static Command fromJson(String json) {
        try {
            // try to map it as an object
            Command obj = gson.fromJson(json, Command.class);

            // check if it is valid
            if (obj != null && !obj.getClass().equals(Command.class)) {
                return obj;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
