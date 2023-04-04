package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.list.Lists;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.AbstractMultiCondition;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Strings;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class RecordStoreCodeTable extends AbstractLoadingCodeTable
  implements RecordDefinitionProxy {

  private static final List<String> DEFAULT_FIELD_NAMES = Arrays.asList("VALUE");

  private boolean allowNullValues = false;

  private List<String> fieldNameAliases = new ArrayList<>();

  private String idFieldName;

  private List<String> orderBy = DEFAULT_FIELD_NAMES;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  private PathName typePath;

  private List<String> valueFieldNames = DEFAULT_FIELD_NAMES;

  private void addCondition(final AbstractMultiCondition logical, final FieldDefinition field,
    Object value) {
    try {
      Condition condition;
      if (value == null) {
        condition = Q.isNull(field);
      } else {
        final Value valueDef = new Value(field, field.toObject(value), true);
        condition = new Equal(field, valueDef);
      }
      logical.addCondition(condition);
    } catch (final Exception e) {
    }
  }

  protected void addEntryRecord(CodeTableData data, final Record code) {
    final String idFieldName = getIdFieldName();
    final Identifier id = code.getIdentifier(idFieldName);
    if (id == null) {
      throw new NullPointerException(idFieldName + "=null for " + code);
    } else {
      clearCache(id);
      final List<Object> values = new ArrayList<>();
      final List<String> valueFieldNames = getValueFieldNames();
      for (final String fieldName : valueFieldNames) {
        Object value = code.getValue(fieldName);
        if (value instanceof SingleIdentifier) {
          final SingleIdentifier identifier = (SingleIdentifier)value;
          value = identifier.getValue(0);
        }
        if (value == null) {
          if (!this.allowNullValues) {
            throw new NullPointerException(valueFieldNames + "=null for " + code);
          }
        }
        values.add(value);
      }
      if (valueFieldNames.size() == 1) {
        data.addEntry(id, values.get(0));
      } else {
        data.addEntry(id, values);
      }
    }
  }

  public void addFieldAlias(final String columnName) {
    this.fieldNameAliases.add(columnName);
  }

  @Override
  public final void addValue(final Record code) {
    addEntryRecord(getData(), code);
  }

  protected void clearCache(Identifier id) {

  }

  @Override
  public RecordStoreCodeTable clone() {
    final RecordStoreCodeTable clone = (RecordStoreCodeTable)super.clone();
    clone.recordDefinition = null;
    clone.fieldNameAliases = new ArrayList<>(this.fieldNameAliases);
    return clone;
  }

  public void forEachRecord(final Consumer<Record> action) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      newQuery().forEachRecord(action);
    }
  }

  @SuppressWarnings("unchecked")
  public <C extends CodeTable> C getCodeTable() {
    return (C)this;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.fieldNameAliases;
  }

  private FieldDefinition getIdField() {
    return getRecordDefinition().getField(getIdFieldName());
  }

  @Override
  public String getIdFieldName() {
    if (this.idFieldName != null) {
      return this.idFieldName;
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition == null) {
        return "";
      } else {
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName == null) {
          return recordDefinition.getFieldName(0);
        } else {
          return idFieldName;
        }
      }
    }
  }

  @Override
  public JsonObject getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    final JsonObject map = JsonObject.hash();
    if (!(values == null || values.isEmpty())) {
      final List<String> valueFieldNames = getValueFieldNames();
      for (int i = 0; i < valueFieldNames.size(); i++) {
        final String name = valueFieldNames.get(i);
        final Object value = values.get(i);
        map.addNotEmpty(name, value);
      }
    }
    return map;
  }

  public List<String> getOrderBy() {
    return this.orderBy;
  }

  @Override
  public Record getRecord(final Identifier id) {
    return getRecordStore().getRecord(this.getTypePath(), id);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public String getTypeName() {
    return this.getTypePath().getPath();
  }

  public PathName getTypePath() {
    return this.typePath;
  }

  @Override
  public int getValueFieldLength() {
    final List<FieldDefinition> valueFieldDefinitions = getValueFieldDefinitions();
    int length = valueFieldDefinitions.size() - 1;
    for (final FieldDefinition field : valueFieldDefinitions) {
      length += field.getLength();
    }
    return length;
  }

  public String getValueFieldName() {
    return this.valueFieldNames.get(0);
  }

  @Override
  public List<String> getValueFieldNames() {
    return Collections.unmodifiableList(this.valueFieldNames);
  }

  public boolean isAllowNullValues() {
    return this.allowNullValues;
  }

  @Override
  public boolean isMultiValue() {
    return this.valueFieldNames.size() > 1;
  }

  @Override
  protected Mono<CodeTableData> loadAll() {
    if (getRecordStore() == null) {
      return Mono.empty();
    } else {
      return Mono.just(new CodeTableData(this))
        .flatMap(data -> newQuery()//
          .fluxForEach()
          .subscribeOn(Schedulers.boundedElastic())
          .doOnNext(record -> addEntryRecord(data, record))
          .then()
          .thenReturn(data));
    }
  }

  @Override
  protected Mono<Boolean> loadValueDo(Object value) {
    final Query query = getRecordStore().newQuery(this.getTypePath());
    if (value == null) {
      for (final FieldDefinition field : getValueFieldDefinitions()) {
        query.and(Q.isNull(field));
      }
    } else {
      final FieldDefinition idField = getIdField();
      final Or or = Q.or();
      addCondition(or, idField, value);

      final And and = Q.and();
      List<?> values;
      if (value instanceof Collection) {
        values = new ArrayList<>((Collection<?>)value);
      } else {
        values = Collections.singletonList(value);
      }
      final int i = 0;
      for (final FieldDefinition field : getValueFieldDefinitions()) {
        final Object listValue;
        if (i < values.size()) {
          listValue = values.get(i);
        } else {
          listValue = null;
        }
        addCondition(and, field, listValue);
      }
      if (!and.isEmpty()) {
        or.addCondition(and);
      }
      if (or.isEmpty()) {
        return Mono.just(false);
      }
      query.and(or);
    }
    query.forEachRecord(record -> addEntryRecord(getData(), record));
    return Mono.just(true);
  }

  @Override
  public Query newQuery() {
    final PathName typePath = getTypePath();
    final String idFieldName = getIdFieldName();
    final List<String> valueFieldNames = getValueFieldNames();
    final List<String> orderBy = getOrderBy();
    final Query query = getRecordStore()//
      .newQuery(typePath)
      .select(idFieldName);
    for (final String name : valueFieldNames) {
      query.select(name);
    }
    for (final String name : orderBy) {
      query.addOrderBy(name);
    }
    return query;
  }

  public AbstractLoadingCodeTable setAllowNullValues(final boolean allowNullValues) {
    this.allowNullValues = allowNullValues;
    return this;
  }

  public AbstractLoadingCodeTable setFieldAliases(final String... fieldNameAliases) {
    setFieldNameAliases(Lists.newArray(fieldNameAliases));
    return this;
  }

  public AbstractLoadingCodeTable setFieldNameAliases(final List<String> fieldNameAliases) {
    this.fieldNameAliases = new ArrayList<>(fieldNameAliases);
    return this;
  }

  public RecordStoreCodeTable setIdFieldName(final String idFieldName) {
    this.idFieldName = idFieldName;
    return this;
  }

  public RecordStoreCodeTable setOrderBy(final List<String> orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  public RecordStoreCodeTable setOrderByFieldName(final String orderByFieldName) {
    this.orderBy = Arrays.asList(orderByFieldName);
    return this;
  }

  public RecordStoreCodeTable setRecordDefinition(final RecordDefinition recordDefinition) {
    if (this.recordDefinition != recordDefinition) {
      if (this.recordDefinition != null) {
        setRecordDefinitionBefore(this.recordDefinition);
      }
      this.recordDefinition = recordDefinition;
      if (recordDefinition == null) {
        this.recordStore = null;
        this.typePath = null;
      } else {
        this.typePath = recordDefinition.getPathName();
        final String name = this.getTypePath().getName();
        setName(name);
        if (this.idFieldName == null) {
          this.idFieldName = recordDefinition.getIdFieldName();
        }
        this.recordStore = this.recordDefinition.getRecordStore();
        final List<FieldDefinition> valueFields = new ArrayList<>();
        for (final String fieldName : getValueFieldNames()) {
          valueFields.add(recordDefinition.getField(fieldName));
        }
        setValueFieldDefinitions(valueFields);
        setRecordDefinitionAfter(recordDefinition);
      }
    }
    return this;
  }

  protected void setRecordDefinitionAfter(final RecordDefinition recordDefinition) {
  }

  protected void setRecordDefinitionBefore(final RecordDefinition oldRecordDefinition) {
  }

  public RecordStoreCodeTable setValueFieldName(final String valueFieldName) {
    this.valueFieldNames = Arrays.asList(valueFieldName);
    if (this.orderBy == DEFAULT_FIELD_NAMES) {
      setOrderByFieldName(valueFieldName);
    }
    return this;
  }

  public RecordStoreCodeTable setValueFieldNames(final List<String> valueColumns) {
    this.valueFieldNames = new ArrayList<>(valueColumns);
    if (this.orderBy == DEFAULT_FIELD_NAMES) {
      setOrderBy(valueColumns);
    }
    return this;
  }

  public RecordStoreCodeTable setValueFieldNames(final String... valueColumns) {
    setValueFieldNames(Arrays.asList(valueColumns));
    return this;
  }

  @Override
  public String toString() {
    return getTypePath() + " " + getIdFieldName() + " "
      + Strings.toString(",", getValueFieldName());

  }
}
