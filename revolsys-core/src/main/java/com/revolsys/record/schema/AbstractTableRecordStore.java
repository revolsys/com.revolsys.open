package com.revolsys.record.schema;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jeometry.common.data.identifier.Identifier;
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
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
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

  protected RecordDefinition recordDefinition;

  protected Map<QueryValue, Boolean> defaultSortOrder = new LinkedHashMap<>();

  protected Query recordsQuery;

  private final Set<String> searchFieldNames = new LinkedHashSet<>();

  public AbstractTableRecordStore(final PathName typePath) {
    this.tablePath = typePath;
    this.typeName = typePath.getName();
  }

  public AbstractTableRecordStore(final PathName typePath, final JdbcRecordStore recordStore) {
    this(typePath);
    setRecordStore(recordStore);
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

  public void applyDefaultSortOrder(final Query query) {
    query.addOrderBy(this.defaultSortOrder);
  }

  public Query applySearchCondition(final Query query, String search) {
    if (!this.searchFieldNames.isEmpty()) {
      if (Property.hasValue(search)) {
        final Or or = new Or();
        search = '%' + search.trim().toLowerCase() + '%';
        for (final String fieldName : this.searchFieldNames) {
          final Condition condition = query.newCondition(fieldName, Q.ILIKE, search);
          or.addCondition(condition);
        }
        query.and(or);
      }
    }
    return query;
  }

  public Map<QueryValue, Boolean> getDefaultSortOrder() {
    return this.defaultSortOrder;
  }

  public Record getRecord(final TableRecordStoreConnection connection, final CharSequence fieldName,
    final Object value) {
    final Query query = newQuery(fieldName, value);
    return getRecord(connection, query);
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> R getRecord(final TableRecordStoreConnection connection,
    final Query query) {
    try (
      Transaction transaction = connection.newTransaction(Propagation.REQUIRED)) {
      return (R)this.recordStore.getRecord(query);
    }
  }

  public Record getRecordById(final TableRecordStoreConnection connection, final UUID id) {
    return getRecord(connection, "id", id);
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

  public boolean hasRecord(final TableRecordStoreConnection connection,
    final CharSequence fieldName, final Object value) {
    final Query query = newQuery(fieldName, value);
    return hasRecord(connection, query);
  }

  public boolean hasRecord(final TableRecordStoreConnection connection, final Query query) {
    try (
      Transaction transaction = connection.newTransaction(Propagation.REQUIRED)) {
      return this.recordStore.getRecordCount(query) > 0;
    }
  }

  public Record insertOrUpdateRecord(final TableRecordStoreConnection connection,
    final Condition condition, final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    final Query query = newQuery()//
      .and(condition);
    return insertOrUpdateRecord(connection, query, newRecordSupplier, updateAction);
  }

  public Record insertOrUpdateRecord(final TableRecordStoreConnection connection, final Query query,
    final Supplier<Record> newRecordSupplier, final Consumer<Record> updateAction) {
    query.setRecordFactory(ArrayChangeTrackRecord.FACTORY).setLockMode(LockMode.FOR_UPDATE);

    final ChangeTrackRecord changeTrackRecord = getRecord(connection, query);
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

  public Query newQuery(final CharSequence fieldName, final Object value) {
    return newQuery() //
      .and(fieldName, Q.EQUAL, value)//
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

  protected void setDefaultSortOrder(final Collection<String> fieldNames) {
    this.defaultSortOrder.clear();
    for (final String fieldName : fieldNames) {
      addDefaultSortOrder(fieldName);
    }

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

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    this.recordsQuery = new Query(this.recordDefinition);
  }

  protected void setRecordStore(final JdbcRecordStore recordStore) {
    this.recordStore = recordStore;
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(this.tablePath);
    setRecordDefinition(recordDefinition);
  }

  public AbstractTableRecordStore setSearchFieldNames(final Collection<String> searchFieldNames) {
    this.searchFieldNames.addAll(searchFieldNames);
    return this;
  }

  public AbstractTableRecordStore setSearchFieldNames(final String... searchFieldNames) {
    for (final String searchFieldName : searchFieldNames) {
      this.searchFieldNames.add(searchFieldName);
    }
    return this;
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Condition condition,
    final Consumer<Record> updateAction) {
    final Query query = newQuery().and(condition);
    return updateRecord(connection, query, updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Identifier id,
    final Consumer<Record> updateAction) {
    final Condition condition = Q.equalId(this.recordDefinition.getIdFieldNames(), id);
    return updateRecord(connection, condition, updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Identifier id,
    final JsonObject values) {
    return updateRecord(connection, id, (record) -> record.setValues(values));
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Query query,
    final Consumer<Record> updateAction) {
    try (
      Transaction transaction = connection.newTransaction(Propagation.REQUIRED)) {
      query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);
      final ChangeTrackRecord record = getRecord(connection, query);
      if (record == null) {
        return null;
      } else {
        updateAction.accept(record);
        updateRecordDo(connection, record);
        return record.newRecord();
      }
    }
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final UUID id,
    final Consumer<Record> updateAction) {
    final Condition condition = this.recordDefinition.equal("id", id);
    return updateRecord(connection, condition, updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final UUID id,
    final JsonObject values) {
    return updateRecord(connection, id, (record) -> record.setValues(values));
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
