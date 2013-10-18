package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;

public interface JdbcDataObjectStore extends DataObjectStore {

  DataObjectStoreQueryReader createReader();

  @Override
  JdbcWriter createWriter();

  Connection getConnection();

  String getDatabaseTableName(final String typePath);
  String getDatabaseQualifiedTableName(final String typePath);

  String getDatabaseSchemaName(final String schemaName);

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

  void releaseWriter(final JdbcWriter writer);

  void setDataSource(DataSource dataSource);

  @Override
  void setLabel(String label);
}
