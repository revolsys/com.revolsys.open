package com.revolsys.record.schema;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.InsertUpdateAction;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOption;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.TransactionRecordReader;

public class RecordStoreQuery extends Query {

  private final RecordStore recordStore;

  public RecordStoreQuery(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public int deleteRecords() {
    try (
      Transaction transaction = this.recordStore.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.deleteRecords(this);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> R getRecord() {
    return (R)this.recordStore.getRecord(this);
  }

  @Override
  public long getRecordCount() {
    return this.recordStore.getRecordCount(this);
  }

  @Override
  public RecordReader getRecordReader() {
    final Transaction transaction = this.recordStore.newTransaction(TransactionOptions.REQUIRED);
    final RecordReader reader = this.recordStore.getRecords(this);
    return new TransactionRecordReader(reader, transaction);
  }

  @Override
  public RecordReader getRecordReader(Transaction transaction) {
    if (transaction == null) {
      transaction = this.recordStore.newTransaction(TransactionOptions.REQUIRED);
    }
    final RecordReader reader = this.recordStore.getRecords(this);
    return new TransactionRecordReader(reader, transaction);
  }

  @Override
  public Record insertOrUpdateRecord(final InsertUpdateAction action) {
    return this.recordStore.insertOrUpdateRecord(this, action);

  }

  @Override
  public Record insertOrUpdateRecord(final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    return this.recordStore.insertOrUpdateRecord(this, newRecordSupplier, updateAction);
  }

  @Override
  public Record insertRecord(final Supplier<Record> newRecordSupplier) {
    return this.recordStore.insertRecord(this, newRecordSupplier);
  }

  @Override
  public Transaction newTransaction() {
    return this.recordStore.newTransaction();
  }

  @Override
  public Transaction newTransaction(final TransactionOption... options) {
    return this.recordStore.newTransaction(options);
  }

  @Override
  public Record updateRecord(final Consumer<Record> updateAction) {
    return this.recordStore.updateRecord(this, updateAction);
  }

  @Override
  public int updateRecords(final Consumer<? super ChangeTrackRecord> updateAction) {
    return this.recordStore.updateRecords(this, updateAction);
  }
}
