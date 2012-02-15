package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

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

  public List<DataObject> getAllObjects(final String... orderBy) {
    final Query query = new Query(tableName);
    String idPropertyName = getIdPropertyName();
    if (orderBy.length > 0) {
      query.setOrderBy(orderBy);
    } else if (StringUtils.hasText(idPropertyName)) {
      query.setOrderBy(idPropertyName);
    }
    final Reader<DataObject> reader = dataStore.query(query);
    return reader.read();
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public ResultPager<DataObject> getResultPager(final Map<String, Object> filter) {
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

  protected boolean isPropertyUnique(
    final DataObject object,
    final String attributeName) {
    final Query query = new Query(tableName);
    final String value = object.getValue(attributeName);
    final Map<String, String> filter = Collections.singletonMap(attributeName,
      value);
    query.setFilter(filter);
    final DataObjectStore dataStore = getDataStore();
    final Reader<DataObject> results = dataStore.query(query);
    final List<DataObject> objects = results.read();
    if (object.getState() == DataObjectState.New) {
      return objects.isEmpty();
    } else {
      final Object id = object.getIdValue();
      for (final Iterator<DataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final DataObject matchedObject = iterator.next();
        final Object matchedId = matchedObject.getIdValue();
        if (EqualsRegistry.INSTANCE.equals(id, matchedId)) {
          iterator.remove();
        }
      }
      return objects.isEmpty();
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
