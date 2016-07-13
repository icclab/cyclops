package ch.icclab.cyclops.load.model;
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

/**
 * Author: Skoviera
 * Created: 27/04/16
 * Description: Hibernate configuration and credentials
 */
public class HibernateCredentials {
    private String hibernateURL;
    private String hibernateUsername;
    private String hibernatePassword;
    private String hibernateDriver;
    private String hibernateDialect;

    public String getHibernateURL() {
        return hibernateURL;
    }
    public void setHibernateURL(String hibernateURL) {
        this.hibernateURL = hibernateURL;
    }

    public String getHibernateUsername() {
        return hibernateUsername;
    }
    public void setHibernateUsername(String hibernateUsername) {
        this.hibernateUsername = hibernateUsername;
    }

    public String getHibernatePassword() {
        return hibernatePassword;
    }
    public void setHibernatePassword(String hibernatePassword) {
        this.hibernatePassword = hibernatePassword;
    }

    public String getHibernateDriver() {
        return hibernateDriver;
    }
    public void setHibernateDriver(String hibernateDriver) {
        this.hibernateDriver = hibernateDriver;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }
    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }
}
