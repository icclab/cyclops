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

import ch.icclab.cyclops.persistence.orm.InstanceORM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import javax.xml.crypto.Data;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 09/02/16
 * Description: Client class for Hibernate
 */
public class HibernateClient {
    final static Logger logger = LogManager.getLogger(HibernateClient.class.getName());

    // singleton
    private static HibernateClient singleton;
    private SessionFactory sessionFactory = null;
    private Configuration configuration = null;

    /**
     * Constructor
     * @param conf to be used
     */
    private HibernateClient(Configuration conf) {
        configuration = conf;
    }

    /**
     * Create Hibernate instance
     * @param conf to be used
     * @return instance
     */
    public static HibernateClient createInstance(Configuration conf){
        if (singleton == null) {
            singleton = new HibernateClient(conf);
        }

        return singleton;
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static HibernateClient getInstance() {
        return singleton;
    }

    /**
     * Mercifully shut down Hibernate
     */
    public static void shutDown() {
        if (singleton != null && singleton.sessionFactory != null) {
            logger.trace("Shutting down Hibernate client");
            singleton.closeSessionFactory();
            singleton = null;
        }
    }

    /**
     * Build session factory
     * @return session factory
     */
    private SessionFactory createSessionFactory(Configuration configuration) {
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    /**
     * Close the session factory
     */
    private void closeSessionFactory() {
        try {
            sessionFactory.close();
        } catch (Exception ignored) {
        }

        sessionFactory = null;
    }

    /**
     * Get session from Session Factory
     * NOTE: Hibernate is not meant to be thread safe, so don't forget to always ask for a session
     * @return session or null
     */
    private Session obtainSession() throws DatabaseException {
        try {
            Session session = sessionFactory.openSession();
            return testSession(session);
        } catch (Exception e) {
            // close the old factory just in case
            closeSessionFactory();

            try {
                // let's reopen the factory (we will try one time only)
                sessionFactory = createSessionFactory(configuration);

                // with the new factory open a session
                Session session = sessionFactory.openSession();
                return testSession(session);
            } catch (Exception f) {
                closeSessionFactory();
                throw new DatabaseException(String.format("couldn't reach database (%s)", f.getMessage()));
            }
        }
    }

    /**
     * Test whether database session is still active
     * @param session to be tested
     * @return Session valid
     * @throws DatabaseException in case that test query fails
     */
    private Session testSession(Session session) throws DatabaseException {
        String reason = "Couldn't reach database";
        try {
            if (session.createNativeQuery(QueryHelper.testQuery()).list() != null) return session;
            else throw new DatabaseException(reason);
        } catch (Exception e) {
            throw new DatabaseException(reason);
        }
    }

    /**
     * Close a session
     * @param session to be closed
     */
    private void closeSession(Session session) {
        try {
            session.close();
        } catch (Exception ignored){}
    }

    /**
     * Let Hibernate store provided object to database
     * @param obj to be stored
     * @return updated object
     */
    public Object persistObject(Object obj) throws DatabaseException {
        // start transaction
        Transaction tx = null;

        // automatically close session
        try (Session session = obtainSession()) {
            tx = session.beginTransaction();

            // persist object
            session.saveOrUpdate(obj);

            // commit it
            tx.commit();

            return obj;
        } catch (DatabaseException e) {
            closeSessionFactory();
            throw new DatabaseException("Couldn't reach database while persisting an object");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException(String.format("Persisting an object failed: %s", e.getMessage()));
        }
    }

    /**
     * Access database and execute query
     * @param hql query
     * @return list
     */
    public List executeQuery(String hql) throws DatabaseException {
        try (Session session = obtainSession()) {
            return session.createQuery(hql).list();
        } catch (DatabaseException e) {
            closeSessionFactory();
            throw new DatabaseException("Couldn't reach database while executing a query");
        } catch (Exception e) {
            throw new DatabaseException(String.format("Query execution failed: %s", e.getMessage()));
        }
    }

    /**
     * Access database and get object
     * @param clazz object
     * @param id of object
     * @return object
     */
    public Object getObject(Class clazz, Long id) throws DatabaseException {
        // get session first
        try (Session session = obtainSession()) {
            return session.get(clazz, id);
        } catch (DatabaseException e) {
            closeSessionFactory();
            throw new DatabaseException("Couldn't reach database while trying to get an object");
        } catch (Exception e) {
            throw new DatabaseException(String.format("Fetching an object failed: %s", e.getMessage()));
        }
    }

    /**
     * Remove object from database
     * @param obj object to be deleted
     */
    public boolean deleteObject(Object obj) throws DatabaseException {
        // start transaction
        Transaction tx = null;

        // automatically close session
        try (Session session = obtainSession()) {
            tx = session.beginTransaction();

            // delete desired object
            session.delete(obj);

            // commit it
            tx.commit();

            return true;
        } catch (DatabaseException e) {
            closeSessionFactory();
            throw new DatabaseException("Couldn't reach database while deleting an object");
        } catch (Exception e) {
            // object not found
            if (tx != null) tx.rollback();
            return false;
        }
    }

    /**
     * Check the database connection by listing instances and waiting for an exception
     * @throws DatabaseException for unsuccessful connection to the database
     */
    public void ping() throws DatabaseException {
        try {
            executeQuery(QueryHelper.createListQuery(InstanceORM.class));
        } catch (Exception e) {
            closeSessionFactory();
            throw new DatabaseException("Unable to connect to database");
        }
    }
}
