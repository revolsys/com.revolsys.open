package com.revolsys.record.schema;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;
import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.TableReference;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.TransactionRecordReader;
import com.revolsys.transaction.Transactionable;

public interface TableRecordStoreConnection extends Transactionable {

  @SuppressWarnings("unchecked")
  default <R extends Record> R getRecord(final Query query) {
    final JdbcRecordStore recordStore = getRecordStore();
    try (
      Transaction transaction = newTransaction(TransactionOptions.REQUIRED)) {
      return (R)recordStore.getRecord(query);
    }
  }

  default long getRecordCount(final Query query) {
    try (
      Transaction transaction = newTransaction(TransactionOptions.REQUIRED)) {
      final JdbcRecordStore recordStore = getRecordStore();
      return recordStore.getRecordCount(query);
    }
  }

  default RecordDefinition getRecordDefinition(final PathName tablePath) {
    final JdbcRecordStore recordStore = getRecordStore();
    return recordStore.getRecordDefinition(tablePath);
  }

  default RecordReader getRecordReader(final PathName tableName) {
    final AbstractTableRecordStore recordStore = getTableRecordStore(tableName);
    final Query query = recordStore.newQuery(this);
    return recordStore.getRecordReader(this, query);
  }

  default RecordReader getRecordReader(final Query query) {
    final JdbcRecordStore recordStore = getRecordStore();
    final Transaction transaction = newTransaction(TransactionOptions.REQUIRED);
    final RecordReader reader = recordStore.getRecords(query);
    return new TransactionRecordReader(reader, transaction);
  }

  default List<Record> getRecords(final Query query) {
    final JdbcRecordStore recordStore = getRecordStore();
    try (
      Transaction transaction = newTransaction(TransactionOptions.REQUIRED);
      RecordReader recordReader = recordStore.getRecords(query)) {
      return recordReader.toList();
    } catch (final Exception e) {
      throw Exceptions.wrap("Query error\n" + query, e);
    }
  }

  JdbcRecordStore getRecordStore();

  default TableReference getTable(final CharSequence pathName) {
    final AbstractTableRecordStore recordStore = getTableRecordStore(pathName);
    if (recordStore == null) {
      return null;
    } else {
      return recordStore.getTable();
    }
  }

  <TRS extends AbstractTableRecordStore> TRS getTableRecordStore(CharSequence pathName);

  default <TRS extends AbstractTableRecordStore> TRS getTableRecordStore(
    final PathNameProxy pathNameProxy) {
    if (pathNameProxy != null) {
      final PathName pathName = pathNameProxy.getPathName();
      return getTableRecordStore(pathName);
    }
    return null;
  }

  @Override
  default PlatformTransactionManager getTransactionManager() {
    final JdbcRecordStore recordStore = getRecordStore();
    return recordStore.getTransactionManager();
  }

  default <TRS extends AbstractTableRecordStore> Record insertOrUpdateRecord(
    final CharSequence tablePath, final Function<TRS, Query> querySupplier,
    final Function<TRS, Record> newRecordSupplier, final Consumer<Record> updateAction) {
    final TRS tableRecordStore = getTableRecordStore(tablePath);
    final Query query = querySupplier.apply(tableRecordStore);
    final Supplier<Record> insertSupplier = () -> newRecordSupplier.apply(tableRecordStore);
    return tableRecordStore.insertOrUpdateRecord(this, query, insertSupplier, updateAction);
  }

  default <TRS extends AbstractTableRecordStore> Record insertRecord(final CharSequence tablePath,
    final Function<TRS, Query> querySupplier, final Function<TRS, Record> newRecordSupplier) {
    final Consumer<Record> updateAction = record -> {
    };
    return insertOrUpdateRecord(tablePath, querySupplier, newRecordSupplier, updateAction);
  }

  default Record insertRecord(final Record record) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(record);
    return tableRecordStore.insertRecord(this, record);
  }

  default Query newQuery(final CharSequence tablePath) {
    final AbstractTableRecordStore recordStore = getTableRecordStore(tablePath);
    return recordStore.newQuery(this);
  }

  default Record newRecord(final CharSequence tablePath) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.newRecord();
  }

  default Record newRecord(final CharSequence tablePath, final JsonObject json) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.newRecord(json);
  }

  default Record updateRecord(final CharSequence tablePath, final Identifier id,
    final Consumer<Record> updateAction) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.updateRecord(this, id, updateAction);
  }

  default Record updateRecord(final CharSequence tablePath, final Identifier id,
    final JsonObject values) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.updateRecord(this, id, values);
  }

  default Record updateRecord(final Record record, final Consumer<Record> updateAction) {
    final PathName tablePath = record.getPathName();
    final Identifier id = record.getIdentifier();
    return updateRecord(tablePath, id, updateAction);
  }
}
