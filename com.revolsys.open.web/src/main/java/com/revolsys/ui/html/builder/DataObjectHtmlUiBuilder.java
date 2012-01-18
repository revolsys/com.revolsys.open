package com.revolsys.ui.html.builder;

import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.Query;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Writer;

public class DataObjectHtmlUiBuilder extends HtmlUiBuilder<DataObject> {

  private DataObjectStore dataStore;

  private QName tableName;

  public DataObjectHtmlUiBuilder() {
  }

  public DataObjectHtmlUiBuilder(final String typeName, final String title) {
    super(typeName, title);
  }

  public DataObjectHtmlUiBuilder(final String typeName, final String title,
    final String pluralTitle) {
    super(typeName, title, pluralTitle);
  }

  @Override
  protected DataObject createObject() {
    return dataStore.create(tableName);
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  protected ResultPager<DataObject> getObjectList(
    final Map<String, Object> filter) {
    final Query query = new Query(tableName);
    query.setFilter(filter);
    return dataStore.page(query);
  }

  public QName getTableName() {
    return tableName;
  }

  @Override
  protected void insertObject(final DataObject object) {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      writer.write(object);
    } finally {
      writer.close();
    }
  }

  @Override
  protected DataObject loadObject(final Object id) {
    final DataObject object = dataStore.load(tableName, id);
    return object;
  }

  public void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setTableName(final QName tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final DataObject object) {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      writer.write(object);
    } finally {
      writer.close();
    }
  }

}
