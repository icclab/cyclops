package ch.icclab.cyclops.persistence;
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

import ch.icclab.cyclops.facts.PersistedFact;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 10/03/16
 * Description: Get list of facts that are being persisted into memory and should be reloaded automatically on server restart
 */
public class PersistedFacts {

    public static List<Class> getListOfPersistedFactClasses() {

        List<Class> persistedFacts = new ArrayList<>();

        try {
            // find all classes annotated as Entity
            String path = PersistedFact.class.getPackage().getName();
            persistedFacts.addAll(new ClassesInPackageScanner().findAnnotatedClasses(path, Entity.class));
        } catch (Exception ignored) {
        }

        return persistedFacts;
    }

}
