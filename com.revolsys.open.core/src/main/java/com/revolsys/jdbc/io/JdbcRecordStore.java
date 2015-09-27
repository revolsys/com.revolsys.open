package com.revolsys.jdbc.io;

import java.sql.ResultSetMetaData;

import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public interface JdbcRecordStore extends RecordStore {
  String getDatabaseQualifiedTableName(PathName typePath);

  String getDatabaseSchemaName(final PathName schemaPath);

  String getDatabaseTableName(final PathName typePath);

  String getGeneratePrimaryKeySql(RecordDefinition recordDefinition);

  JdbcConnection getJdbcConnection();

  JdbcConnection getJdbcConnection(boolean autoCommit);

  Object getNextPrimaryKey(RecordDefinition recordDefinition);

  Object getNextPrimaryKey(String typePath);

  RecordDefinition getRecordDefinition(String tableName, ResultSetMetaData resultSetMetaData);
}
