package com.revolsys.gis.data.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class DelegatingDataObjectStore implements DataObjectStore {

  private DataObjectStore dataObjectStore;

  public DelegatingDataObjectStore() {
  }

  public DelegatingDataObjectStore(final DataObjectStore dataObjectStore) {
    this.dataObjectStore = dataObjectStore;
  }

  public void close() {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.close();
  }

  public DataObject create(final QName typeName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.create(typeName);
  }

  protected DataObjectStore createDataObjectStore() {
    throw new UnsupportedOperationException("Data store must be set manually");
  }

  public Writer<DataObject> createWriter() {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.createWriter();
  }

  public void delete(final DataObject object) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.delete(object);
  }

  public void deleteAll(final Collection<DataObject> objects) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.deleteAll(objects);
  }

  public CodeTable getCodeTable(final QName typeName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getCodeTable(typeName);
  }

  public CodeTable getCodeTableByColumn(final String columnName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getCodeTableByColumn(columnName);
  }

  public DataObjectFactory getDataObjectFactory() {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getDataObjectFactory();
  }

  public DataObjectStore getDataObjectStore() {
    if (dataObjectStore == null) {
      dataObjectStore = createDataObjectStore();
    }
    return dataObjectStore;
  }

  public DataObjectMetaData getMetaData(final QName typeName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getMetaData(typeName);
  }

  public Map<String, Object> getProperties() {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getProperties();
  }

  @SuppressWarnings("unchecked")
  public <C> C getProperty(final String name) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return (C)dataObjectStore.getProperty(name);
  }

  public DataObjectStoreSchema getSchema(final String schemaName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getSchema(schemaName);
  }

  public List<DataObjectStoreSchema> getSchemas() {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getSchemas();
  }

  public List<QName> getTypeNames(final String namespace) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getTypeNames(namespace);
  }

  public List<DataObjectMetaData> getTypes(final String namespace) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getTypes(namespace);
  }

  public void insert(final DataObject object) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.insert(object);
  }

  public void insertAll(final Collection<DataObject> objects) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.insertAll(objects);
  }

  public boolean isEditable(final QName typeName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.isEditable(typeName);
  }

  public DataObject load(final QName typeName, final Object id) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.load(typeName, id);
  }

  public Reader<DataObject> query(final QName typeName) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName);
  }

  public Reader<DataObject> query(final QName typeName,
    final BoundingBox boundingBox) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName, boundingBox);
  }

  public Reader<DataObject> query(final QName typeName, final Envelope envelope) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName, envelope);
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName, geometry);
  }

  public DataObject query(final QName typeName, final String queryString,
    final Object... arguments) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName, queryString, arguments);
  }

  protected void setDataObjectStore(final DataObjectStore dataObjectStore) {
    this.dataObjectStore = dataObjectStore;
  }

  public void setProperty(final String name, final Object value) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.setProperty(name, value);
  }

  public void update(final DataObject object) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.update(object);
  }

  public void updateAll(final Collection<DataObject> objects) {
    final DataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.updateAll(objects);
  }

}
