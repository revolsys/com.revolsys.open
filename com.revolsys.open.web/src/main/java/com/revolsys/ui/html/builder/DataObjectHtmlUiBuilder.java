package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.Reader;
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

  protected boolean isPropertyUnique(DataObject object, String attributeName) {
    Query query = new Query(tableName);
    String value = object.getValue(attributeName);
    Map<String, String> filter = Collections.singletonMap(attributeName, value);
    query.setFilter(filter);
    DataObjectStore dataStore = getDataStore();
    Reader<DataObject> results = dataStore.query(query);
    List<DataObject> objects = results.read();
    if (object.getState() == DataObjectState.New) {
      return objects.isEmpty();
    } else {
      Object id = object.getIdValue();
      for (Iterator<DataObject> iterator = objects.iterator(); iterator.hasNext();) {
        DataObject matchedObject = iterator.next();
        Object matchedId = matchedObject.getIdValue();
        if (EqualsRegistry.INSTANCE.equals(id, matchedId)) {
          iterator.remove();
        }
      }
      return objects.isEmpty();
    }

  }

  @Override
  public ResultPager<DataObject> getObjectList(
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
      if (object.getIdValue() == null) {
        object.setIdValue(dataStore.createPrimaryIdValue(tableName));
      }
      writer.write(object);
    } finally {
      writer.close();
    }
  }

  @Override
  public DataObject loadObject(final Object id) {
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
