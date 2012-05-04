package com.revolsys.ui.html.builder;

import java.util.List;
import java.util.Map;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataAccessObject;

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

  public DataAccessObject<T> getDataAccessObject() {
    return dataAccessObject;
  }

  public List<T> getList(final Map<String, Object> filter) {
    return getList(filter, getOrderBy());
  }

  public List<T> getList(
    final Map<String, Object> filter,
    final Map<String, Boolean> orderBy) {
    return dataAccessObject.list(filter, orderBy);
  }

  public Class<T> getObjectClass() {
    return objectClass;
  }

  @Override
  public ResultPager<T> getResultPager(final Map<String, Object> filter) {
    return getResultPager(filter, getOrderBy());
  }

  public ResultPager<T> getResultPager(
    final Map<String, Object> filter,
    final Map<String, Boolean> orderBy) {
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
