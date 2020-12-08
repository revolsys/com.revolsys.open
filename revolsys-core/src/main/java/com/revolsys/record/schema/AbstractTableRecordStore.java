package com.revolsys.record.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.util.Property;

public class AbstractTableRecordStore implements RecordDefinitionProxy {

  protected JdbcRecordStore recordStore;

  protected final PathName tablePath;

  protected final String typeName;

  protected RecordDefinition recordDefinition;

  protected Map<QueryValue, Boolean> defaultSortOrder = new LinkedHashMap<>();

  protected Query recordsQuery;

  private final List<FieldDefinition> updateFields = new ArrayList<>();

  private final List<String> nonUpdateFieldNames = Lists.newArray("id", "projectId",
    "createTimestamp", "createUserId");

  public AbstractTableRecordStore(final PathName typePath) {
    this.tablePath = typePath;
    this.typeName = typePath.getName();
  }

  public AbstractTableRecordStore(final PathName typePath, final JdbcRecordStore recordStore) {
    this(typePath);
    setRecordStore(recordStore);
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
      updateCachedFields();
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

  private void updateCachedFields() {
    this.updateFields.clear();
    final List<FieldDefinition> fields = this.recordDefinition.getFields();
    for (final FieldDefinition field : fields) {
      final String fieldName = field.getName();
      if (field.isGenerated()) {
      } else if (this.nonUpdateFieldNames.contains(fieldName)) {
      } else {
        this.updateFields.add(field);
      }
    }
  }

  public void validateRecord(final MapEx record) {
    this.recordDefinition.validateRecord(record);
  }

}
