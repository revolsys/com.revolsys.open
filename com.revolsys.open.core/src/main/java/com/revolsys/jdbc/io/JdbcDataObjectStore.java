package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.Reader;
import com.vividsolutions.jts.geom.Geometry;

public interface JdbcDataObjectStore extends DataObjectStore {

  DataObjectStoreQueryReader createReader();

  @Override
  JdbcWriter createWriter();

  Connection getConnection();

  DataSource getDataSource();

  String getGeneratePrimaryKeySql(DataObjectMetaData metaData);

  @Override
  String getLabel();

  DataObjectMetaData getMetaData(String tableName,
    ResultSetMetaData resultSetMetaData);

  Object getNextPrimaryKey(DataObjectMetaData metaData);

  Object getNextPrimaryKey(String typePath);

  Statistics getStatistics(String name);

  @Override
  void initialize();

  Reader<DataObject> query(String path, Geometry geometry, String condition);

  void releaseWriter(final JdbcWriter writer);

  void setDataSource(DataSource dataSource);

  @Override
  void setLabel(String label);
}
