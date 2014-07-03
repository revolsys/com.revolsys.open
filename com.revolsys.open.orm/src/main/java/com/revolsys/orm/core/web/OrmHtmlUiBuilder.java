package com.revolsys.orm.core.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.io.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.builder.HtmlUiBuilder;

public class OrmHtmlUiBuilder<T> extends HtmlUiBuilder<T> {
  private DataAccessObject<T> dataAccessObject;

  private Class<T> objectClass;

  public OrmHtmlUiBuilder() {
  }

  public OrmHtmlUiBuilder(final String typeName) {
    setTypeName(typeName);
  }

  public OrmHtmlUiBuilder(final String typeName, final String title) {
    super(typeName, title);
  }

  public OrmHtmlUiBuilder(final String typeName, final String title,
    final String pluralTitle) {
    super(typeName, title, pluralTitle);
  }

  @Override
  protected T createObject() {
    try {
      return objectClass.newInstance();
    } catch (final InstantiationException e) {
      throw new RuntimeException("Unable to instantiate " + objectClass, e);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Unable to instantiate " + objectClass, e);
    }
  }

  /**
   * Get the Data Access Object for the object's class.
   * 
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  public <V> DataAccessObject<V> getDao(final Class<V> objectClass) {
    final BeanFactory beanFactory = getBeanFactory();
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClass);
    } else {
      return null;
    }
  }

  /**
   * Get the Data Access Object for the object's class name.
   * 
   * @param objectClass<?>Column The object class name.
   * @return The builder.
   */
  public <V> DataAccessObject<V> getDao(final String objectClassName) {
    final BeanFactory beanFactory = getBeanFactory();
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClassName);
    } else {
      return null;
    }
  }

  public DataAccessObject<T> getDataAccessObject() {
    return dataAccessObject;
  }

  public List<T> getList(final Map<String, Object> filter) {
    final Map<String, Boolean> orderBy = Collections.emptyMap();
    return dataAccessObject.list(filter, orderBy);
  }

  public Class<T> getObjectClass() {
    return objectClass;
  }

  @Override
  public ResultPager<T> getResultPager(final Map<String, Object> filter) {
    final Map<String, Boolean> orderBy = Collections.emptyMap();
    return dataAccessObject.page(filter, orderBy);
  }

  @Override
  protected void insertObject(final T object) {
    dataAccessObject.persist(object);
    dataAccessObject.flush();
  }

  @Override
  public T loadObject(final Object id) {
    try {
      final long longId = Long.parseLong(id.toString());
      final T object = dataAccessObject.load(longId);
      return object;
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  protected void lockObject(final T object) {
    dataAccessObject.lockAndRefresh(object);
  }

  public void setDataAccessObject(final DataAccessObject<T> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public void setObjectClass(final Class<T> objectClass) {
    this.objectClass = objectClass;
  }

  @Override
  public void setRollbackOnly(final T object) {
    dataAccessObject.evict(object);
  }

  @Override
  protected void updateObject(final T object) {
    dataAccessObject.flush();
  }

}
