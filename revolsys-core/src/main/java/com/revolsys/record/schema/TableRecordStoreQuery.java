package com.revolsys.record.schema;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;

public class TableRecordStoreQuery extends Query {

  private final AbstractTableRecordStore recordStore;

  private final TableRecordStoreConnection connection;

  public TableRecordStoreQuery(final AbstractTableRecordStore recordStore,
    final TableRecordStoreConnection connection) {
    super(recordStore.getRecordDefinition());
    this.recordStore = recordStore;
    this.connection = connection;
  }

  public int deleteRecords() {
    try (
      Transaction transaction = this.connection.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.getRecordStore().deleteRecords(this);
    }
  }

  @Override
  public int deleteRecords(final TableRecordStoreConnection connection, final Query query) {
    return this.recordStore.deleteRecords(this.connection, this);
  }

  @Override
  public <R extends Record> R getRecord() {
    return this.recordStore.getRecord(this.connection, this);
  }

  @Override
  public long getRecordCount() {
    return this.recordStore.getRecordCount(this.connection, this);
  }

  @Override
  public RecordReader getRecordReader() {
    return this.recordStore.getRecordReader(this.connection, this);
  }
}
