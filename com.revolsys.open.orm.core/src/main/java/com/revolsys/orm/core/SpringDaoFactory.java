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
package com.revolsys.orm.core;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SpringDaoFactory implements BeanFactoryAware,
  DataAccessObjectFactory {
  /** The springframework bean factory used to get the Data Access Objects. */
  private BeanFactory beanFactory;

  /**
   * Get the {@link DataAccessObject} from the bean factory. The Data Access
   * Object will be loaded from the bean objectClass.name+"-dao".
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param factory The springframework bean factory used to get the Data Access
   *          Objects.
   * @param objectClass The class of the entity to get the Data Access Object
   *          for.
   * @return The Data Access Object instance.
   */
  @SuppressWarnings("unchecked")
  public static <T extends DataAccessObject<?>> T get(
    final BeanFactory factory,
    final Class<?> objectClass) {
    return (T)get(factory, objectClass.getName());
  }

  /**
   * Get the {@link DataAccessObject} from the bean factory. The Data Access
   * Object will be loaded from the bean objectClassName+"-dao".
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param factory The springframework bean factory used to get the Data Access
   *          Objects.
   * @param objectClassName The name of the class of the entity to get the Data
   *          Access Object for.
   * @return The Data Access Object instance.
   */
  @SuppressWarnings("unchecked")
  public static <T extends DataAccessObject<?>> T get(
    final BeanFactory factory,
    final String objectClassName) {
    Object bean = factory.getBean(objectClassName + "-dao");
    return (T)get(bean);
  }

  /**
   * Get the Data Access Object from the bean. If the bean is a
   * {@link DaoProxyFactory} the
   * {@link DaoProxyFactory#createDataAccessObject(String)} method will be
   * invoked to create the Data Access Object instance. If the bean is an
   * instance of {@link DataAccessObject} it will be returned. Any other types
   * of objects will cause an {@link IllegalArgumentException} to be thrown.
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param bean The bean.
   * @return The DataAccessObject instance.
   */
  @SuppressWarnings("unchecked")
  private static <T extends DataAccessObject<?>> T get(
    final Object bean) {
    if (bean instanceof DaoProxyFactory) {
      DaoProxyFactory proxyFactory = (DaoProxyFactory)bean;
      return (T)proxyFactory.createDataAccessObject(null);
    } else if (bean instanceof DataAccessObject<?>) {
      return (T)bean;
    }
    throw new IllegalArgumentException(bean.getClass().getName()
      + " is not a valid data access object");
  }

  /**
   * Construct a new SpringDaoFactory.
   */
  public SpringDaoFactory() {
  }

  /**
   * Construct a new SpringDaoFactory.
   * 
   * @param beanFactory The springframework bean factory used to get the Data
   *          Access Objects.
   */
  public SpringDaoFactory(
    final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Set the springframework bean factory used to get the Data Access Objects.
   * 
   * @param beanFactory The springframework bean factory used to get the Data
   *          Access Objects.
   */
  public void setBeanFactory(
    final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Get the {@link DataAccessObject} from the bean factory. The Data Access
   * Object will be loaded from the bean "dao:" + objectClass.name.
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param objectClass The class of the entity to get the Data Access Object
   *          for.
   * @return The Data Access Object instance.
   */
  @SuppressWarnings("unchecked")
  public <T extends DataAccessObject<?>> T get(
    final Class<?> objectClass) {
    return (T)get(beanFactory, objectClass.getName());
  }

  /**
   * Get the {@link DataAccessObject} from the bean factory. The Data Access
   * Object will be loaded from the bean "dao:" + objectClassName.
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param objectClassName The name of the class of the entity to get the Data
   *          Access Object for.
   * @return The Data Access Object instance.
   */
  @SuppressWarnings("unchecked")
  public <T extends DataAccessObject<?>> T get(
    final String objectClassName) {
    return (T)get(beanFactory, objectClassName);
  }
}
