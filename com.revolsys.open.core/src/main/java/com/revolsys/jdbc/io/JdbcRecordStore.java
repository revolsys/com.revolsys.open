package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreQueryReader;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.Statistics;

public interface JdbcRecordStore extends RecordStore {

  RecordStoreQueryReader createReader();

  @Override
  JdbcWriter createWriter();

  Connection getConnection();

  String getDatabaseQualifiedTableName(final String typePath);

  String getDatabaseSchemaName(final String schemaName);

  String getDatabaseTableName(final String typePath);

  DataSource getDataSource();

  String getGeneratePrimaryKeySql(RecordDefinition recordDefinition);

  @Override
  String getLabel();

  RecordDefinition getRecordDefinition(String tableName,
    ResultSetMetaData resultSetMetaData);

  Object getNextPrimaryKey(RecordDefinition recordDefinition);

  Object getNextPrimaryKey(String typePath);

  Statistics getStatistics(String name);

  @Override
  void initialize();

  void setDataSource(DataSource dataSource);

  @Override
  void setLabel(String label);
}
