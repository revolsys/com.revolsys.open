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
package com.revolsys.orm.jpa.dao;

import java.lang.reflect.Proxy;

import javax.persistence.EntityManagerFactory;

import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.orm.core.DaoProxyFactory;
import com.revolsys.orm.core.DomainClass;

/**
 * @author Paul Austin
 */
public class JpaDaoProxyFactory implements DaoProxyFactory {
  private EntityManagerFactory entityManagerFactory;

  @SuppressWarnings("unchecked")
  public <T> DataAccessObject<T> createDataAccessObject(
    final String daoInterfaceClassName) {

    Class<?> daoInterface = getDaoInterface(daoInterfaceClassName);

    DomainClass domainClassAnnotation = (DomainClass)daoInterface.getAnnotation(DomainClass.class);
    Class<?> domainClass = domainClassAnnotation.value();

    JpaDaoHandler handler = new JpaDaoHandler(daoInterface, domainClass);
    handler.setEntityManagerFactory(entityManagerFactory);

    ClassLoader classLoader = domainClass.getClassLoader();
    Class<?>[] params = new Class<?>[] {
      daoInterface
    };
    DataAccessObject<T> dataAccessObject = (DataAccessObject<T>)Proxy.newProxyInstance(
      classLoader, params, handler);
    return dataAccessObject;
  }

  /**
   * Get the value of the OBJECT_CLASS field from a Data Access Object
   * interface.
   * 
   * @param daoInterfaceName The class name of the Data Access Object interface.
   * @return The Class definition for the objects that the Data Access Object
   *         manages.
   */
  private Class<?> getDaoInterface(final String daoInterfaceName) {
    try {
      Class<?> daoInterface = Class.forName(daoInterfaceName);
      return daoInterface;
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("DaoClass " + daoInterfaceName
        + " could not be found");
    }
  }

  public EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

  public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }
}
