package com.revolsys.jdbc.io;

import com.revolsys.io.PathName;
import com.revolsys.record.schema.RecordStoreSchema;

public class JdbcRecordStoreSchema extends RecordStoreSchema {

  private String dbName;

  public JdbcRecordStoreSchema(final AbstractJdbcRecordStore recordStore) {
    super(recordStore);
  }

  public JdbcRecordStoreSchema(final JdbcRecordStoreSchema schema, final PathName pathName,
    final String dbName) {
    super(schema, pathName);
    this.dbName = dbName;
  }

  public String getDbName() {
    return this.dbName;
  }
}
