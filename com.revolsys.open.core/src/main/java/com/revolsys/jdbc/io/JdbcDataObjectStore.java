package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Reader;
import com.vividsolutions.jts.geom.Geometry;

public interface JdbcDataObjectStore extends DataObjectStore {

  DataObjectStoreQueryReader createReader();

  JdbcWriter createWriter();

  Connection getConnection();

  DataSource getDataSource();

  String getGeneratePrimaryKeySql(DataObjectMetaData metaData);

  String getLabel();

  DataObjectMetaData getMetaData(String tableName,
    ResultSetMetaData resultSetMetaData);

  Object getNextPrimaryKey(DataObjectMetaData metaData);

  Object getNextPrimaryKey(String typePath);

  void initialize();

  Reader<DataObject> query(String path, Geometry geometry, String condition);

  void setDataSource(DataSource dataSource);

  void setLabel(String label);

  void releaseWriter(final JdbcWriter writer);
}
