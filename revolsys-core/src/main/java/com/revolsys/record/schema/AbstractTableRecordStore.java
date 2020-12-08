package com.revolsys.record.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.MapEx;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.ArrayChangeTrackRecord;
import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;

public class AbstractTableRecordStore implements RecordDefinitionProxy {

  protected JdbcRecordStore recordStore;

  protected final PathName tablePath;

  protected final String typeName;

  protected final RecordDefinition recordDefinition;

  protected Map<QueryValue, Boolean> defaultSortOrder = new LinkedHashMap<>();

  protected final Query recordsQuery;

  public AbstractTableRecordStore(final PathName typePath, final JdbcRecordStore recordStore) {
    this.recordStore = recordStore;
    this.tablePath = typePath;
    this.typeName = typePath.getName();
    this.recordDefinition = this.recordStore.getRecordDefinition(typePath);
    this.recordsQuery = new Query(this.recordDefinition);

  }

  public void addDefaultSortOrder(final Query query) {
    query.addOrderBy(this.defaultSortOrder);
  }

  protected void addDefaultSortOrder(final String fieldName) {
    addDefaultSortOrder(fieldName, true);
  }

  protected void addDefaultSortOrder(final String fieldName, final boolean ascending) {
    final FieldDefinition field = getFieldDefinition(fieldName);
    if (field == null) {
      throw new IllegalArgumentException("field not found: " + fieldName);
    } else {
      this.defaultSortOrder.put(field, ascending);
    }
  }

  public Map<QueryValue, Boolean> getDefaultSortOrder() {
    return this.defaultSortOrder;
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> R getRecord(final Query query) {
    return (R)this.recordStore.getRecord(query);
  }

  public Record getRecordById(final UUID id) {
    final Query query = newQuery("id", id);
    return getRecord(query);
  }

  public int getRecordCount(final Query query) {
    return this.recordStore.getRecordCount(query);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordReader getRecords() {
    return this.recordStore.getRecords(this.recordsQuery);
  }

  public RecordReader getRecords(final Condition condition) {
    final Query query = newQuery()//
      .and(condition);
    return getRecords(query);
  }

  protected RecordReader getRecords(final Query query) {
    return this.recordStore.getRecords(query);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public TableReference getTable() {
    return this.recordDefinition;
  }

  public PathName getTablePath() {
    return this.tablePath;
  }

  public Record insertOrUpdateRecord(final TableRecordStoreConnection connection,
    final Condition condition, final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    final Query query = newQuery()//
      .and(condition)
      .setRecordFactory(ArrayChangeTrackRecord.FACTORY)
      .setLockMode(LockMode.FOR_UPDATE);

    final ChangeTrackRecord changeTrackRecord = getRecord(query);
    if (changeTrackRecord == null) {
      final Record newRecord = newRecordSupplier.get();
      if (newRecord == null) {
        return null;
      } else {
        return insertRecord(connection, newRecord);
      }
    } else {
      updateAction.accept(changeTrackRecord);
      updateRecordDo(connection, changeTrackRecord);
      return changeTrackRecord.newRecord();
    }
  }

  public Record insertRecord(final TableRecordStoreConnection connection, final Record record) {
    try (
      Transaction transaction = connection.newTransaction(Propagation.REQUIRED)) {
      insertRecordBefore(connection, record);
      validateRecord(record);
      this.recordStore.insertRecord(record);
      insertRecordAfter(connection, record);
    }
    return record;
  }

  protected void insertRecordAfter(final TableRecordStoreConnection connection,
    final Record record) {
  }

  protected void insertRecordBefore(final TableRecordStoreConnection connection,
    final Record record) {
  }

  @Override
  public Query newQuery() {
    return this.recordDefinition.newQuery();
  }

  public Query newQuery(final String fieldName, final Object value) {
    final Condition equal = this.recordDefinition.equal(fieldName, value);
    return newQuery() //
      .and(equal)//
    ;
  }

  public Record newRecord() {
    return this.recordDefinition.newRecord();
  }

  public Record newRecord(final MapEx values) {
    if (values == null) {
      return null;
    } else {
      final Record record = newRecord();
      for (final String fieldName : values.keySet()) {
        final Object value = values.getValue(fieldName);
        if (Property.hasValue(value)) {
          record.setValue(fieldName, value);
        }
      }
      return record;
    }
  }

  public UUID newUUID() {
    return UUID.randomUUID();
  }

  public void setDefaultSortOrder(final String... fieldNames) {
    this.defaultSortOrder.clear();
    for (final String fieldName : fieldNames) {
      addDefaultSortOrder(fieldName);
    }
  }

  protected void setGeneratedFields(final String... fieldNames) {
    if (this.recordDefinition != null) {
      for (final String fieldName : fieldNames) {
        final JdbcFieldDefinition field = (JdbcFieldDefinition)this.recordDefinition
          .getField(fieldName);
        if (field != null) {
          field.setGenerated(true);
        }
      }
    }
  }

  public Record updateRecord(final TableRecordStoreConnection tenant, final Condition condition,
    final Consumer<Record> updateAction) {
    try (
      Transaction transaction = tenant.newTransaction(Propagation.REQUIRED)) {
      final Query query = newQuery().and(condition);
      query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);
      final ChangeTrackRecord record = getRecord(query);
      if (record == null) {
        return null;
      } else {
        updateAction.accept(record);
        updateRecordDo(tenant, record);
        return record.newRecord();
      }
    }
  }

  public Record updateRecord(final TableRecordStoreConnection tenant, final UUID id,
    final Consumer<Record> updateAction) {
    final Condition condition = this.recordDefinition.equal("id", id);
    return updateRecord(tenant, condition, updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection tenant, final UUID id,
    final JsonObject values) {
    return updateRecord(tenant, id, (record) -> record.setValues(values));
  }

  protected void updateRecordAfter(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
  }

  protected void updateRecordBefore(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
  }

  private void updateRecordDo(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
    if (record.isModified()) {
      updateRecordBefore(connection, record);
      this.recordStore.updateRecord(record);
      updateRecordAfter(connection, record);
    }
  }

  public void validateRecord(final MapEx record) {
    this.recordDefinition.validateRecord(record);
  }

}
