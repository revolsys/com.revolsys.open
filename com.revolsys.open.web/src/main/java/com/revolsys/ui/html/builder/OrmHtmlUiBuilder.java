package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Map;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataAccessObject;

public class OrmHtmlUiBuilder<T> extends HtmlUiBuilder<T> {
  private DataAccessObject<T> dataAccessObject;

  private Class<T> objectClass;

  public OrmHtmlUiBuilder() {
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

  public Class<T> getObjectClass() {
    return objectClass;
  }

  @Override
  protected ResultPager<T> getObjectList(final Map<String, Object> filter) {
    return dataAccessObject.page(filter, Collections.singletonMap("id", true));
  }

  @Override
  protected void insertObject(final T object) {
    dataAccessObject.persist(object);
    dataAccessObject.flush();
  }

  @Override
  protected T loadObject(final Object id) {
    try {
      final long longId = Long.parseLong(id.toString());
      final T object = dataAccessObject.load(longId);
      return object;
    } catch (final NumberFormatException e) {
      return null;
    }
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
