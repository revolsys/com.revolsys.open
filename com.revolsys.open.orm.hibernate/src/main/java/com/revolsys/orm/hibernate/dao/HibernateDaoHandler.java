/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.orm.hibernate.dao;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.orm.core.NamedQueryParameter;
import com.revolsys.orm.hibernate.callback.HibernateNamedQueryCallback;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;

public class HibernateDaoHandler extends HibernateDaoSupport implements
  InvocationHandler {
  /** The class definition of the DataAcessObject interface. */
  private Class<?> daoInterface;

  /** The class definition of the entities persisted by this Data Access Object. */
  private Class<?> objectClass;

  /** The class name of the entities persisted by this Data Access Object. */
  private String objectClassName;

  public HibernateDaoHandler(
    final Class<?> daoInterface,
    final Class<?> objectClass) {
    this.daoInterface = daoInterface;
    this.objectClass = objectClass;
    this.objectClassName = objectClass.getName();
  }

  /**
   * Clear all objects loaded from persistent storage from the cache. After
   * invoking this method the in memory Java objects will be disconnected from
   * the persistent storage and any changes to them will not be saved.
   */
  public void clearCache() {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.clear();
  }

  public Object createInstance() {
    try {
      return objectClass.newInstance();
    } catch (Exception e) {
      return ExceptionUtil.throwCauseException(e);
    }
  }

  /**
   * Evict the object from cache of managed objects. After evicting an object
   * not further changes to the object will be saved to the persistent storage.
   * 
   * @param object The object to evict.
   * @return null.
   */
  public Object evict(
    final Object object) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.evict(object);
    return null;
  }

  public List find(
    final Method method,
    final String queryName,
    final Object[] args) {
    try {
      Query query = getQuery(method, queryName, args);
      return query.list();
    } catch (HibernateException e) {
      throw SessionFactoryUtils.convertHibernateAccessException(e);
    }
  }

  public List findMax(
    final Method method,
    final String queryName,
    final int limit,
    final Object[] args) {
    try {
      Query query = getQuery(method, queryName, args);
      query.setMaxResults(limit);
      return query.list();
    } catch (HibernateException e) {
      throw SessionFactoryUtils.convertHibernateAccessException(e);
    }
  }

  /**
   * Flush all changes to the persistent storage.
   */
  public void flush() {
    getHibernateTemplate().flush();
  }

  public Object get(
    final Method method,
    final String queryName,
    final Object[] args) {
    try {
      Query query = getQuery(method, queryName, args);
      Iterator resultIter = query.iterate();
      if (resultIter.hasNext()) {
        return resultIter.next();
      } else {
        return null;
      }
    } catch (HibernateException e) {
      throw SessionFactoryUtils.convertHibernateAccessException(e);
    }
  }

  private Query getQuery(
    final Method method,
    final String queryName,
    final Object... args) {
    Object[] arguments;
    if (args == null) {
      arguments = new Object[0];
    } else {
      arguments = args;
    }
    String fullQueryName = getQueryName(queryName);
    HibernateCallback<Query> callback = new HibernateNamedQueryCallback(fullQueryName);
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    Query query = hibernateTemplate.execute(callback);
    for (int i = 0; i < arguments.length; i++) {
      String name = getQueryParameterName(method, i);
      Object value = arguments[i];
      if (name == null) {
        query.setParameter(i, value);
      } else {
        if (value instanceof Collection<?>) {
          query.setParameterList(name, (Collection<?>)value);
        } else if (value instanceof Object[]) {
          query.setParameterList(name, (Object[])value);
        } else if (value instanceof Enum<?>) {
          query.setParameter(name, value.toString());
        } else {
          query.setParameter(name, value);
        }
      }
    }
    return query;
  }

  public String getQueryName(
    final String methodName) {
    String queryName = methodName.replaceFirst(
      "\\A(get|page|findMax|find|iterate|delete|update)", "");
    return objectClassName + "." + queryName;
  }

  private String getQueryParameterName(
    final Method method,
    final int index) {
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (Annotation annotation : parameterAnnotations[index]) {
      if (annotation.annotationType().equals(NamedQueryParameter.class)) {
        String parameterName = ((NamedQueryParameter)annotation).value();
        return parameterName;
      }
    }
    return null;
  }

  public Object invoke(
    final Object proxy,
    final Method method,
    final Object[] args)
    throws Throwable {
    SessionFactory sessionFactory = getSessionFactory();
    boolean participate = false;

    if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
      // Do not modify the Session: just set the participate flag.
      participate = true;
    } else {
      logger.debug("Creating Hibernate Session");
      Session session = SessionFactoryUtils.getSession(sessionFactory, true);
      session.setFlushMode(FlushMode.AUTO);
      TransactionSynchronizationManager.bindResource(sessionFactory,
        new SessionHolder(session));
    }
    try {

      String methodName = method.getName();
      try {
        Class<?>[] paramTypes = method.getParameterTypes();
        Method localMethod = getClass().getMethod(methodName, paramTypes);
        return localMethod.invoke(this, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      } catch (SecurityException e) {
        throw e;
      } catch (NoSuchMethodException e) {
        if (methodName.equals("removeAll")) {
          return removeAll((Collection<Object>)args[0]);
        } else if (methodName.startsWith("remove")) {
          if (args[0] instanceof Long) {
            return remove((Long)args[0]);
          } else {
            return remove(args[0]);
          }
        } else if (methodName.startsWith("findMax")) {
          Object[] newArgs = new Object[args.length - 1];
          System.arraycopy(args, 1, newArgs, 0, newArgs.length);
          int limit = (Integer)args[0];
          return findMax(method, methodName, limit, newArgs);
        } else if (methodName.startsWith("find")) {
          return find(method, methodName, args);
        } else if (methodName.startsWith("iterate")) {
          return iterate(method, methodName, args);
        } else if (methodName.startsWith("get")) {
          return get(method, methodName, args);
        } else if (methodName.startsWith("page")) {
          return page(method, methodName, args);
        } else if (methodName.startsWith("persist")) {
          return persist(args[0]);
        } else if (methodName.startsWith("load")) {
          return load(args[0]);
        } else if (methodName.startsWith("delete")) {
          return delete(method, methodName, args);
        } else if (methodName.startsWith("update")) {
          return update(method, methodName, args);
        } else {
          throw new IllegalArgumentException("Method " + methodName
            + " does not exist");
        }
      }
    }

    finally {
      if (!participate) {
        SessionHolder sessionHolder = (SessionHolder)TransactionSynchronizationManager.unbindResource(sessionFactory);
        logger.debug("Closing single Hibernate Session");
        SessionFactoryUtils.closeSession(sessionHolder.getSession());
      }
    }

  }

  public Iterator iterate(
    final Method method,
    final String queryName,
    final Object[] args) {
    try {
      Query query = getQuery(method, queryName, args);
      return query.iterate();
    } catch (HibernateException e) {
      throw SessionFactoryUtils.convertHibernateAccessException(e);
    }
  }

  /**
   * Load the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return The object.
   */
  public Object load(
    final Object id) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    Object object = hibernateTemplate.get(objectClass, (Serializable)id);
    return object;
  }

  /**
   * Load the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return The object.
   */
  public Object loadAndLock(
    final Object id) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    Object object = hibernateTemplate.get(objectClass, (Serializable)id,
      LockMode.UPGRADE);
    return object;
  }

  /**
   * Create a lock on the object so that no other transactions can modifiy the
   * object.
   * 
   * @param object The object to lock.
   * @return null.
   */
  public Object lock(
    final Object object) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.lock(object, LockMode.UPGRADE);
    return null;
  }

  @SuppressWarnings("unchecked")
  public <V> List<V> list(
    final String propertyName,
    final Map<String, Object> where,
    final Map<String, Boolean> orderBy) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    return (List<V>)hibernateTemplate.executeWithNativeSession(new HibernateCallback() {
      public Object doInHibernate(
        Session session)
        throws HibernateException {
        Criteria criteria = session.createCriteria(objectClass);
        criteria.setProjection(Projections.distinct(Projections.property(propertyName)));
        if (where != null) {
          for (Entry<String, Object> criterion : where.entrySet()) {
            String key = criterion.getKey();
            Object value = criterion.getValue();
            if (value == null) {
              criteria.add(Restrictions.isNull(key));
            } else if (value instanceof Collection) {
              Collection<?> collection = (Collection<?>)value;
              criteria.add(Restrictions.in(key, collection));
            } else if (value instanceof Object[]) {
              Object[] array = (Object[])value;
              criteria.add(Restrictions.in(key, array));
            } else if (value instanceof Enum) {
              criteria.add(Restrictions.eq(key, value.toString()));
            } else {
              if (value.toString().indexOf("%") == -1) {
                criteria.add(Restrictions.eq(key, value));
              } else {
                criteria.add(Restrictions.ilike(key, value));
              }
            }
          }
        }
        if (orderBy != null) {
          for (Entry<String, Boolean> entry : orderBy.entrySet()) {
            String propertyName = entry.getKey();
            Boolean ascending = entry.getValue();
            if (Boolean.TRUE.equals(ascending)) {
              criteria.addOrder(Order.asc(propertyName));
            } else {
              criteria.addOrder(Order.desc(propertyName));
            }
          }
        }
        return criteria.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public <V> List<V> list(
    final String propertyName,
    final Map<String, Object> where,
    final Map<String, Boolean> orderBy,
    final int limit) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    return (List<V>)hibernateTemplate.executeWithNativeSession(new HibernateCallback() {
      public Object doInHibernate(
        Session session)
        throws HibernateException {
        Criteria criteria = session.createCriteria(objectClass);
        criteria.setProjection(Projections.distinct(Projections.property(propertyName)));
        criteria.setMaxResults(limit);
        if (where != null) {
          for (Entry<String, Object> criterion : where.entrySet()) {
            String key = criterion.getKey();
            Object value = criterion.getValue();
            if (value == null) {
              criteria.add(Restrictions.isNull(key));
            } else if (value instanceof Collection) {
              Collection<?> collection = (Collection<?>)value;
              criteria.add(Restrictions.in(key, collection));
            } else if (value instanceof Object[]) {
              Object[] array = (Object[])value;
              criteria.add(Restrictions.in(key, array));
            } else if (value instanceof Enum) {
              criteria.add(Restrictions.eq(key, value.toString()));
            } else {
              if (value.toString().indexOf("%") == -1) {
                criteria.add(Restrictions.eq(key, value));
              } else {
                criteria.add(Restrictions.ilike(key, value));
              }
            }
          }
        }
        if (orderBy != null) {
          for (Entry<String, Boolean> entry : orderBy.entrySet()) {
            String propertyName = entry.getKey();
            Boolean ascending = entry.getValue();
            if (Boolean.TRUE.equals(ascending)) {
              criteria.addOrder(Order.asc(propertyName));
            } else {
              criteria.addOrder(Order.desc(propertyName));
            }
          }
        }
        return criteria.list();
      }
    });
  }

  public ResultPager page(
    final Map<String, Object> where,
    final Map<String, Boolean> orderBy) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    return (ResultPager)hibernateTemplate.executeWithNativeSession(new HibernateCallback() {
      public Object doInHibernate(
        Session session)
        throws HibernateException {
        Criteria criteria = session.createCriteria(objectClass);
        if (where != null) {
          for (Entry<String, Object> criterion : where.entrySet()) {
            String key = criterion.getKey();
            Object value = criterion.getValue();
            if (value == null) {
              criteria.add(Restrictions.isNull(key));
            } else if (value instanceof Collection) {
              Collection<?> collection = (Collection<?>)value;
              criteria.add(Restrictions.in(key, collection));
            } else if (value instanceof Object[]) {
              Object[] array = (Object[])value;
              criteria.add(Restrictions.in(key, array));
            } else if (value instanceof Enum) {
              criteria.add(Restrictions.eq(key, value.toString()));
            } else {
              if (value.toString().indexOf("%") == -1) {
                criteria.add(Restrictions.eq(key, value));
              } else {
                criteria.add(Restrictions.ilike(key, value));
              }
            }
          }
        }
        if (orderBy != null) {
          for (Entry<String, Boolean> entry : orderBy.entrySet()) {
            String propertyName = entry.getKey();
            Boolean ascending = entry.getValue();
            if (Boolean.TRUE.equals(ascending)) {
              criteria.addOrder(Order.asc(propertyName));
            } else {
              criteria.addOrder(Order.desc(propertyName));
            }
          }
        }
        return new HibernateCriteriaPager(criteria);
      }
    });
  }

  public ResultPager page(
    final Method method,
    final String queryName,
    final Object[] args) {
    Query query = getQuery(method, queryName, args);
    return new HibernateQueryPager(query);

  }

  /**
   * Insert a new object to the persistent storage and get the ID for the
   * object.
   * 
   * @param object The object to insert.
   * @return The ID of the object in the persistent storage.
   */
  public Long persist(
    final Object object) {
    Long id = (Long)getHibernateTemplate().save(object);
    return id;
  }

  /**
   * Refresh the values of the object from the database.
   * 
   * @param object The object to refresh.
   * @return null.
   */
  public Object refresh(
    final Object object) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.refresh(object);
    return null;
  }

  /**
   * Delete the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return null.
   */
  public Object remove(
    final Long id) {
    Object object = load(id);
    return remove(object);
  }

  /**
   * Delete the object from persistent storage.
   * 
   * @param object The object to delete.
   * @return null.
   */
  public Object remove(
    final Object object) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.delete(object);
    return null;
  }

  /**
   * Delete all objects in the collection from persistent storage.
   * 
   * @param objects The list of objects to delete.
   * @return null.
   */
  public Object removeAll(
    final Collection objects) {
    HibernateTemplate hibernateTemplate = getHibernateTemplate();
    hibernateTemplate.deleteAll(objects);
    return null;
  }

  /**
   * Set a configuration property on the DataAccessObject implementation.
   * 
   * @param property The name of the property to set.
   * @param value The value for the property.
   */
  public void setProperty(
    final String property,
    final Object value) {
    JavaBeanUtil.setProperty(this, property, value);
  }

  public Object update(
    final Method method,
    final String queryName,
    final Object[] args) {
    Query query = getQuery(method, queryName, args);
    return (Integer)query.executeUpdate();
  }

  public Object delete(
    final Method method,
    final String queryName,
    final Object[] args) {
    Query query = getQuery(method, queryName, args);
    return (Integer)query.executeUpdate();
  }

  /**
   * Save the values of an updated object in the persistent storage.
   * 
   * @param object The object to update.
   * @return null.
   */
  public Object update(
    final Object object) {
    getHibernateTemplate().update(object);
    return null;
  }
}
