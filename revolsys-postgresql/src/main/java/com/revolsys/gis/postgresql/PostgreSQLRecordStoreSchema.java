package com.revolsys.gis.postgresql;

import org.jeometry.common.io.PathName;

import com.revolsys.jdbc.io.JdbcRecordStoreSchema;

public class PostgreSQLRecordStoreSchema extends JdbcRecordStoreSchema {

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStore recordStore) {
    super(recordStore);
  }

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStoreSchema schema,
    final PathName pathName, final String dbName) {
    super(schema, pathName, dbName);
  }

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStoreSchema schema,
    final PathName pathName, final String dbName, final boolean quoteName) {
    super(schema, pathName, dbName, quoteName);
  }

}
