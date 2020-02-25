package com.revolsys.jdbc.io;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordDefinitionImpl;

public class JdbcRecordDefinition extends RecordDefinitionImpl {

  private final String dbTableName;

  private final String dbTableQualifiedName;

  public JdbcRecordDefinition(final JdbcRecordStoreSchema schema, final PathName pathName,
    final String dbTableName) {
    super(schema, pathName);
    this.dbTableName = dbTableName;

    final String dbSchemaName = schema.getDbName();
    if (dbSchemaName == null) {
      this.dbTableQualifiedName = dbTableName;
    } else {
      this.dbTableQualifiedName = dbSchemaName + "." + dbTableName;
    }
  }

  public String getDbSchemaName() {
    final JdbcRecordStoreSchema schema = getSchema();
    return schema.getDbName();
  }

  public String getDbTableName() {
    return this.dbTableName;
  }

  public String getDbTableQualifiedName() {
    return this.dbTableQualifiedName;
  }

  @Override
  public JdbcRecordDefinition rename(final String path) {
    final JdbcRecordStoreSchema schema = getSchema();
    final PathName pathName = PathName.newPathName(path);
    return new JdbcRecordDefinition(schema, pathName, this.dbTableName);
  }
}
