package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public interface JdbcRecordStore extends RecordStore {

  String getGeneratePrimaryKeySql(JdbcRecordDefinition recordDefinition);

  JdbcConnection getJdbcConnection();

  JdbcConnection getJdbcConnection(boolean autoCommit);

  JdbcRecordDefinition getRecordDefinition(String tableName, ResultSetMetaData resultSetMetaData,
    String dbTableName);

  PreparedStatement insertStatementPrepareRowId(JdbcConnection connection,
    RecordDefinition recordDefinition, String sql) throws SQLException;

  boolean isIdFieldRowid(RecordDefinition recordDefinition);
}
