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

package ch.icclab.cyclops.load;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Author: Skoviera
 * Created: 21/01/16
 * Description: Implementation of loading and parsing configuration file, as well as providing required getters
 * Warning: Because it's singleton, but it requires a context, the first time you create
 */
public class Loader {
    final static Logger logger = LogManager.getLogger(Loader.class.getName());

    // singleton pattern
    private static Loader singleton;

    // loaded settings and environment
    private Settings settings;

    /**
     * Constructor has to be private, as we are using singleton
     */
    private Loader(String path) throws Exception {
        // only if object is created by createInstance (which gives it context)
        if (!path.isEmpty()) {
            // start with loading config file
            Properties properties = loadAndParseConfigurationFile(path);

            if (properties == null) {
                throw new Exception();
            }

            settings = new Settings(properties);
        }
    }

    /**
     * When creating instance, we need it to have context
     * @param path for the configuration file
     */
    public static void createInstance(String path) throws Exception {
        if (singleton == null) {
            singleton = new Loader(path);
        }
    }

    /**
     * Access settings from Loader class
     * @return settings object or null
     */
    public static Settings getSettings() {
        if (singleton.settings != null) {
            return singleton.settings;
        } else {
            return null;
        }
    }

    /**
     * Load and parse configuration file
     * @return null or property object
     */
    private Properties loadAndParseConfigurationFile(String path) {
        // store everything into property variable
        Properties prop = new Properties();

        try {
            // load file
            InputStream input = new FileInputStream(path);

            // now feed input file to property loader
            prop.load(input);

            return prop;

        } catch (FileNotFoundException e) {
            logger.error("Configuration file doesn't exist or cannot be loaded: " + e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("Couldn't load configuration file: " + e.getMessage());
            return null;
        }
    }
}