package com.revolsys.swing.map.layer.record;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOption;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.TransactionRecordReader;

public class RecordLayerQuery extends Query {

  private final AbstractRecordLayer layer;

  public RecordLayerQuery(final AbstractRecordLayer layer) {
    this.layer = layer;
  }

  @Override
  public int deleteRecords() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> R getRecord() {
    return (R)this.layer.getRecord(this);
  }

  @Override
  public long getRecordCount() {
    return this.layer.getRecordCount(this);
  }

  @Override
  public RecordReader getRecordReader() {
    final Transaction transaction = newTransaction(TransactionOptions.REQUIRED);
    final RecordReader reader = this.layer.getRecordReader(this);
    return new TransactionRecordReader(reader, transaction);
  }

  @Override
  public RecordReader getRecordReader(Transaction transaction) {
    if (transaction == null) {
      transaction = newTransaction(TransactionOptions.REQUIRED);
    }
    final RecordReader reader = this.layer.getRecordReader(this);
    return new TransactionRecordReader(reader, transaction);
  }

  @Override
  public Record insertOrUpdateRecord(final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Record insertRecord(final Supplier<Record> newRecordSupplier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Transaction newTransaction() {
    return this.layer.newTransaction();
  }

  @Override
  public Transaction newTransaction(final TransactionOption... options) {
    return this.layer.newTransaction(options);
  }

  @Override
  public Record updateRecord(final Consumer<Record> updateAction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int updateRecords(final Consumer<? super ChangeTrackRecord> updateAction) {
    throw new UnsupportedOperationException();
  }
}
