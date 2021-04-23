package com.revolsys.record.schema;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;

public class TableRecordStoreQuery extends Query {

  private final AbstractTableRecordStore recordStore;

  private final TableRecordStoreConnection connection;

  public TableRecordStoreQuery(final AbstractTableRecordStore recordStore,
    final TableRecordStoreConnection connection) {
    super(recordStore.getRecordDefinition());
    this.recordStore = recordStore;
    this.connection = connection;
  }

  @Override
  public <R extends Record> R getRecord() {
    return this.recordStore.getRecord(this.connection, this);
  }

  @Override
  public RecordReader getRecordReader() {
    return this.recordStore.getRecordReader(this.connection, this);
  }
}
