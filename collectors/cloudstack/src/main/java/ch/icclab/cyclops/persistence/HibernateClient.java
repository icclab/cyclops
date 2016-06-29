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

package ch.icclab.cyclops.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

/**
 * Author: Skoviera
 * Created: 09/02/16
 * Description: Client class for Hibernate
 */
public class HibernateClient {
    final static Logger logger = LogManager.getLogger(HibernateClient.class.getName());

    // singleton
    private static HibernateClient singleton;
    private SessionFactory sessionFactory = null;

    /**
     * Create Hibernate instance
     * @param conf to be used
     */
    public static void createInstance(Configuration conf){
        if (singleton == null) {
            singleton = new HibernateClient(conf);
        }
    }

    /**
     * Constructor
     * @param conf to be used
     */
    private HibernateClient(Configuration conf) {
        sessionFactory = conf.buildSessionFactory();
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static HibernateClient getInstance() {
        return singleton;
    }

    /**
     * Get session from Session Factory
     * NOTE: Hibernate is not meant to be thread safe, so don't forget to always ask for a session
     * @return session
     */
    private Session obtainSession() {
        return sessionFactory.openSession();
    }

    /**
     * Let Hibernate store provided object to database
     * @param obj to be stored
     * @return updated object
     */
    public Object persistObject(Object obj) {

        // first get session
        Session session = obtainSession();

        // start transaction
        session.beginTransaction();

        // persist object
        session.saveOrUpdate(obj);

        // commit it
        session.getTransaction().commit();

        // close connection
        session.flush();
        session.close();

        return obj;
    }

    /**
     * Access database and execute query
     * @param hql query
     * @return list
     */
    public List executeQuery(String hql) {

        // get session first
        Session session = obtainSession();

        // prepare query
        Query query = session.createQuery(hql);

        // get list
        List list = query.list();

        // close session
        session.close();

        return list;
    }

    /**
     * Access database and get object
     * @param clazz object
     * @param id of object
     * @return object
     */
    public Object getObject(Class clazz, Long id) {
        // get session first
        Session session = obtainSession();

        // retrieve object
        Object obj = session.get(clazz, id);

        // close connection
        session.close();

        return obj;
    }

    /**
     * Remove object from database
     * @param obj object to be deleted
     */
    public Boolean deleteObject(Object obj) {

        try {
            // get session
            Session session = obtainSession();

            // now delete it
            session.delete(obj);

            // close
            session.flush();
            session.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mercifully shut down Hibernate
     */
    public static void shutDown() {
        if (singleton != null && singleton.sessionFactory != null) {
            logger.trace("Shutting down Hibernate client");
            singleton.sessionFactory.close();
        }
    }
}
