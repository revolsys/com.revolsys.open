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
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.ArrayChangeTrackRecord;
import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.TransactionRecordReader;
import com.revolsys.util.Property;

public class AbstractTableRecordStore implements RecordDefinitionProxy {

  private JdbcRecordStore recordStore;

  private final PathName tablePath;

  private final String typeName;

  private RecordDefinition recordDefinition;

  protected Map<QueryValue, Boolean> defaultSortOrder = new LinkedHashMap<>();

  private Query recordsQuery;

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
    if (getRecordDefinition() != null) {
      final FieldDefinition field = getFieldDefinition(fieldName);
      if (field != null) {
        this.defaultSortOrder.put(field, ascending);
      }
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
          final ColumnReference column = query.getTable().getColumn(fieldName);
          QueryValue left = column;
          if (column.getDataType() != DataTypes.STRING) {
            left = new Cast(left, "text");
          }
          final Condition condition = query.newCondition(left, Q.ILIKE, search);
          or.addCondition(condition);
        }
        query.and(or);
      }
    }
    return query;
  }

  public int deleteRecords(final TableRecordStoreConnection connection, final Condition condition) {
    final Query query = newQuery().and(condition);
    return deleteRecords(connection, query);
  }

  public int deleteRecords(final TableRecordStoreConnection connection, final Query query) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.deleteRecords(query);
    }
  }

  protected void executeUpdate(final TableRecordStoreConnection connection, final String sql,
    final Object... parameters) {
    try (
      Transaction transaction = connection.newTransaction()) {
      this.recordStore.executeUpdate(sql, parameters);
    }
  }

  public Map<QueryValue, Boolean> getDefaultSortOrder() {
    return this.defaultSortOrder;
  }

  protected JdbcConnection getJdbcConnection() {
    return this.recordStore.getJdbcConnection();
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
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED_READONLY)) {
      return (R)this.recordStore.getRecord(query);
    }
  }

  public Record getRecordById(final TableRecordStoreConnection connection, final UUID id) {
    return getRecord(connection, "id", id);
  }

  public long getRecordCount(final TableRecordStoreConnection connection, final Query query) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED_READONLY)) {
      return this.recordStore.getRecordCount(query);
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordReader getRecordReader(final TableRecordStoreConnection connection) {
    return getRecordReader(connection, this.recordsQuery);
  }

  public RecordReader getRecordReader(final TableRecordStoreConnection connection,
    final Condition condition) {
    final Query query = newQuery().and(condition);
    return getRecordReader(connection, query);
  }

  public RecordReader getRecordReader(final TableRecordStoreConnection connection,
    final Query query) {
    final Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED);
    final RecordReader reader = this.recordStore.getRecords(query);
    return new TransactionRecordReader(reader, transaction);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public TableReference getTable() {
    return getRecordDefinition();
  }

  public PathName getTablePath() {
    return this.tablePath;
  }

  protected String getTypeName() {
    return this.typeName;
  }

  public boolean hasRecord(final TableRecordStoreConnection connection,
    final CharSequence fieldName, final Object value) {
    final Query query = newQuery(fieldName, value);
    return hasRecord(connection, query);
  }

  public boolean hasRecord(final TableRecordStoreConnection connection, final Query query) {
    return getRecordCount(connection, query) > 0;
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

    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
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
  }

  public Record insertRecord(final TableRecordStoreConnection connection, final Query query,
    final Supplier<Record> newRecordSupplier) {
    query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);

    final ChangeTrackRecord changeTrackRecord = getRecord(connection, query);
    if (changeTrackRecord == null) {
      final Record newRecord = newRecordSupplier.get();
      if (newRecord == null) {
        return null;
      } else {
        return insertRecord(connection, newRecord);
      }
    } else {
      return changeTrackRecord.newRecord();
    }
  }

  public Record insertRecord(final TableRecordStoreConnection connection, final Record record) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
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
    return getRecordDefinition().newQuery();
  }

  public Query newQuery(final CharSequence fieldName, final Object value) {
    return newQuery() //
      .and(fieldName, Q.EQUAL, value)//
    ;
  }

  public Record newRecord() {
    return getRecordDefinition().newRecord();
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

  public Transaction newTransaction() {
    return this.recordStore.newTransaction();
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
    if (getRecordDefinition() != null) {
      for (final String fieldName : fieldNames) {
        final JdbcFieldDefinition field = (JdbcFieldDefinition)getRecordDefinition()
          .getField(fieldName);
        if (field != null) {
          field.setGenerated(true);
        }
      }
    }
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (recordDefinition == null) {
      Logs.error(this, "Table doesn't exist\t" + getTypeName());
    } else {
      this.recordsQuery = new Query(getRecordDefinition());
      setRecordDefinitionPost(recordDefinition);
    }
  }

  protected void setRecordDefinitionPost(final RecordDefinition recordDefinition) {
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
    final Condition condition = Q.equalId(getRecordDefinition().getIdFieldNames(), id);
    return updateRecord(connection, condition, updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Identifier id,
    final JsonObject values) {
    return updateRecord(connection, id, (record) -> record.setValues(values));
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Query query,
    final Consumer<Record> updateAction) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
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
    getRecordDefinition().validateRecord(record);
  }

}
