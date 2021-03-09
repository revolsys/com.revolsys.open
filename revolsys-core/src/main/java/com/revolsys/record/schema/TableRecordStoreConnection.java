package com.revolsys.record.schema;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.TableReference;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public interface TableRecordStoreConnection {

  default Record getRecord(final Query query) {
    try (
      Transaction transaction = newTransaction(Propagation.REQUIRED)) {
      final JdbcRecordStore recordStore = getRecordStore();
      return recordStore.getRecord(query);
    }
  }

  default long getRecordCount(final Query query) {
    try (
      Transaction transaction = newTransaction(Propagation.REQUIRED)) {
      final JdbcRecordStore recordStore = getRecordStore();
      return recordStore.getRecordCount(query);
    }
  }

  default RecordDefinition getRecordDefinition(final PathName tablePath) {
    final JdbcRecordStore recordStore = getRecordStore();
    return recordStore.getRecordDefinition(tablePath);
  }

  default RecordReader getRecordReader(final PathName tableName) {
    Transaction.assertInTransaction();
    final AbstractTableRecordStore recordStore = getTableRecordStore(tableName);
    final Query query = recordStore.newQuery();
    return recordStore.getRecords(this, query);
  }

  default RecordReader getRecordReader(final Query query) {
    Transaction.assertInTransaction();
    final JdbcRecordStore recordStore = getRecordStore();
    return recordStore.getRecords(query);
  }

  default List<Record> getRecords(final Query query) {
    try (
      Transaction transaction = newTransaction(Propagation.REQUIRED);
      RecordReader recordReader = getRecordStore().getRecords(query)) {
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
    final Consumer<Record> updateAction = (record) -> {
    };
    return this.insertOrUpdateRecord(tablePath, querySupplier, newRecordSupplier, updateAction);
  }

  default Record insertRecord(final Record record) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(record);
    return tableRecordStore.insertRecord(this, record);
  }

  default Query newQuery(final CharSequence tablePath) {
    final AbstractTableRecordStore recordStore = getTableRecordStore(tablePath);
    return recordStore.newQuery();
  }

  default Record newRecord(final CharSequence tablePath) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.newRecord();
  }

  default Record newRecord(final CharSequence tablePath, final JsonObject json) {
    final AbstractTableRecordStore tableRecordStore = getTableRecordStore(tablePath);
    return tableRecordStore.newRecord(json);
  }

  default Transaction newTransaction() {
    return newTransaction(Propagation.REQUIRES_NEW);
  }

  default Transaction newTransaction(final Propagation propagation) {
    final JdbcRecordStore recordStore = getRecordStore();
    return recordStore.newTransaction(propagation);
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
