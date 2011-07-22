package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DelegatingDataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.io.Reader;
import com.vividsolutions.jts.geom.Geometry;

public class JdbcDelegatingDataObjectStore extends DelegatingDataObjectStore
  implements JdbcDataObjectStore {

  public JdbcDelegatingDataObjectStore() {
  }

  public JdbcDelegatingDataObjectStore(final JdbcDataObjectStore dataObjectStore) {
    super(dataObjectStore);
  }

  public void addCodeTable(final AbstractCodeTable jdbcCodeTableProperty) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.addCodeTable(jdbcCodeTableProperty);
  }

  @Override
  public JdbcWriter createWriter() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.createWriter();
  }

  public Connection getConnection() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getConnection();
  }

  @Override
  public JdbcDataObjectStore getDataObjectStore() {
    return (JdbcDataObjectStore)super.getDataObjectStore();
  }

  public DataSource getDataSource() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getDataSource();
  }

  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getGeneratePrimaryKeySql(metaData);
  }

  public DataObjectMetaData getMetaData(final QName tableName,
    final ResultSetMetaData resultSetMetaData) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getMetaData(tableName, resultSetMetaData);
  }

  public long getNextPrimaryKey(final DataObjectMetaData metaData) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getNextPrimaryKey(metaData);
  }

  public long getNextPrimaryKey(final String typeName) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getNextPrimaryKey(typeName);
  }

  public void initialize() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.initialize();
  }

  public void setDataSource(final DataSource dataSource) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.setDataSource(dataSource);
  }

  public void setLabel(final String label) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.setLabel(label);
  }

  public void setDataObjectFactory(DataObjectFactory dataObjectFactory) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    dataObjectStore.setDataObjectFactory(dataObjectFactory);
  }

  public JdbcQueryReader createReader() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.createReader();
  }

  public Reader<DataObject> query(QName typeName, Geometry geometry,
    String condition) {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.query(typeName, geometry, condition);
  }

  public String getLabel() {
    final JdbcDataObjectStore dataObjectStore = getDataObjectStore();
    return dataObjectStore.getLabel();
  }

  @Override
  public String toString() {
    return getLabel();
  }
}
