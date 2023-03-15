package com.revolsys.record.code;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.ListIdentifier;
import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.reactive.Reactive;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class SingleValueRecordStoreCodeTable extends AbstractSingleValueCodeTable
  implements RecordDefinitionProxy {

  private static final String DEFAULT_FIELD_NAME = "VALUE";

  private boolean allowNullValues = false;

  private boolean createMissingCodes = true;

  private String creationTimestampFieldName;

  private List<String> fieldNameAliases = new ArrayList<>();

  private String idFieldName;

  private boolean loadAll = true;

  private Disposable loadAllDisposable;

  private boolean loaded = false;

  private boolean loadMissingCodes = true;

  private String modificationTimestampFieldName;

  private String orderBy = DEFAULT_FIELD_NAME;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  private PathName typePath;

  private String valueFieldName = DEFAULT_FIELD_NAME;

  public SingleValueRecordStoreCodeTable() {
  }

  public SingleValueRecordStoreCodeTable(final Map<String, ? extends Object> config) {
    setProperties(config);
  }

  public void addFieldAlias(final String columnName) {
    this.fieldNameAliases.add(columnName);
  }

  @Override
  public final void addValue(final Record code) {
    addValueDo(code);
    clearCaches();
  }

  protected void addValueDo(final Record code) {
    final String idFieldName = getIdFieldName();
    final Identifier id = code.getIdentifier(idFieldName);
    if (id == null) {
      throw new NullPointerException(idFieldName + "=null for " + code);
    } else {
      Object value = code.getValue(this.valueFieldName);
      if (value instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)value;
        value = identifier.getValue(0);
      }
      if (value == null) {
        if (!this.allowNullValues) {
          throw new NullPointerException(this.valueFieldName + "=null for " + code);
        }
      }
      addValue(id, value);
    }
  }

  @Override
  protected int calculateValueFieldLength() {
    return this.recordDefinition.getFieldLength(this.valueFieldName);
  }

  protected void clearCaches() {
  }

  @Override
  public SingleValueRecordStoreCodeTable clone() {
    final SingleValueRecordStoreCodeTable clone = (SingleValueRecordStoreCodeTable)super.clone();
    clone.recordDefinition = null;
    clone.fieldNameAliases = new ArrayList<>(this.fieldNameAliases);
    return clone;
  }

  public void forEachRecord(final Consumer<Record> action) {
    final RecordStore recordStore = this.recordStore;
    if (recordStore != null) {
      newQuery().forEachRecord(action);
    }
  }

  @SuppressWarnings("unchecked")
  public <C extends CodeTable> C getCodeTable() {
    return (C)this;
  }

  public String getCreationTimestampFieldName() {
    return this.creationTimestampFieldName;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.fieldNameAliases;
  }

  @Override
  public String getIdFieldName() {
    if (this.idFieldName != null) {
      return this.idFieldName;
    } else if (this.recordDefinition == null) {
      return "";
    } else {
      final String idFieldName = this.recordDefinition.getIdFieldName();
      if (idFieldName == null) {
        return this.recordDefinition.getFieldName(0);
      } else {
        return idFieldName;
      }
    }
  }

  @Override
  public JsonObject getMap(final Identifier id) {
    final Object value = getValue(id);
    if (value == null) {
      return JsonObject.hash();
    } else {
      return JsonObject.hash(this.valueFieldName, value);
    }
  }

  public String getModificationTimestampFieldName() {
    return this.modificationTimestampFieldName;
  }

  @Override
  public Record getRecord(final Identifier id) {
    return this.recordStore.getRecord(this.typePath, id);
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
    return this.typePath.getPath();
  }

  public PathName getTypePath() {
    return this.typePath;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public <V> V getValue(final Object id) {
    if (id instanceof Identifier) {
      return getValue((Identifier)id);
    } else if (id instanceof List) {
      final List list = (List)id;
      return getValue(new ListIdentifier(list));
    } else {
      return getValue(Identifier.newIdentifier(id));
    }
  }

  public String getValueFieldName() {
    return this.valueFieldName;
  }

  @Override
  public List<String> getValueFieldNames() {
    return Collections.singletonList(this.valueFieldName);
  }

  public boolean isAllowNullValues() {
    return this.allowNullValues;
  }

  public boolean isCreateMissingCodes() {
    return this.createMissingCodes;
  }

  @Override
  public boolean isLoadAll() {
    return this.loadAll;
  }

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public boolean isLoading() {
    return this.loadAllDisposable != null;
  }

  @Override
  public boolean isLoadMissingCodes() {
    return this.loadMissingCodes;
  }

  public void loadAll() {
    synchronized (this) {
      if (this.loadAllDisposable != null) {
        if (!this.loadAllDisposable.isDisposed()) {
          this.loadAllDisposable.dispose();
        }
      }
      this.loadAllDisposable = null;

    }
    if (this.recordStore != null) {
      final Flux<Record> publisher = newQuery()//
        .fluxForEach()
        .subscribeOn(Schedulers.boundedElastic())
        .doOnComplete(() -> {
          this.loaded = true;
          Property.firePropertyChange(this, "valuesChanged", false, true);
        });
      Reactive.waitOnAction(publisher, this::addValueDo, disposable -> {
        synchronized (this) {
          this.loadAllDisposable = disposable;
        }
      });
    }

  }

  @Override
  protected synchronized Identifier loadId(CodeTableData data, final Object value,
    final boolean createId) {
    if (this.loadAll && !this.loadMissingCodes && !isEmpty()) {
      return null;
    }
    Identifier id = null;
    if (createId && this.loadAll && !isLoaded()) {
      loadAll();
      id = getIdentifier(data, value, false);
    } else {
      final Query query = this.recordStore.newQuery(this.typePath);
      final And and = new And();
      if (value == null) {
        and.and(Q.isNull(this.valueFieldName));
      } else {
        final FieldDefinition idField = this.recordDefinition.getField(this.idFieldName);
        final FieldDefinition valueField = this.recordDefinition.getField(this.valueFieldName);
        final Or or = Q.or();
        try {
          final Value valueCondition = new Value(idField, idField.toObject(value), true);
          final Equal equal = new Equal(idField, valueCondition);
          or.addCondition(equal);
        } catch (final Exception e) {
        }
        try {
          final Value valueCondition = new Value(valueField, valueField.toObject(value), true);
          final Equal equal = new Equal(valueField, valueCondition);
          or.addCondition(equal);
        } catch (final Exception e) {
        }
        if (or.isEmpty()) {
          return null;
        }
        and.and(or);
      }
      query.setWhereCondition(and);
      query.forEachRecord(this::addValueDo);

      id = getIdByValue(value);
      Property.firePropertyChange(this, "valuesChanged", false, true);
    }
    if (createId && id == null) {
      return newIdentifier(value);
    } else {
      return id;
    }
  }

  @Override
  protected Object loadValue(final Object id) {
    if (this.loadAll && !isLoaded()) {
      loadAll();
    } else if (!this.loadAll || this.loadMissingCodes) {
      try {
        final Record code;
        if (id instanceof Identifier) {
          final Identifier identifier = (Identifier)id;
          code = this.recordStore.getRecord(this.typePath, identifier);
        } else {
          code = this.recordStore.getRecord(this.typePath, id);
        }
        if (code != null) {
          addValue(code);
        }
      } catch (final Throwable e) {
        Logs.error(this, e);
        return null;
      }
    }
    return getValueById(id);
  }

  protected synchronized Identifier newIdentifier(final Object value) {
    if (this.createMissingCodes) {
      // TODO prevent duplicates from other threads/processes
      final Record code = this.recordStore.newRecord(this.typePath);
      final RecordDefinition recordDefinition = code.getRecordDefinition();
      Identifier id = this.recordStore.newPrimaryIdentifier(this.typePath);
      if (id == null) {
        final FieldDefinition idField = recordDefinition.getIdField();
        if (idField != null) {
          if (Number.class.isAssignableFrom(idField.getDataType().getJavaClass())) {
            id = Identifier.newIdentifier(this.getData().getNextId());
          } else {
            id = Identifier.newIdentifier(UUID.randomUUID().toString());
          }
        }
      }
      code.setIdentifier(id);
      code.setValue(this.valueFieldName, value);

      final Instant now = Instant.now();
      if (this.creationTimestampFieldName != null) {
        code.setValue(this.creationTimestampFieldName, now);
      }
      if (this.modificationTimestampFieldName != null) {
        code.setValue(this.modificationTimestampFieldName, now);
      }

      this.recordStore.insertRecord(code);
      return code.getIdentifier();
    } else {
      return null;
    }
  }

  @Override
  public Query newQuery() {
    return this.recordStore//
      .newQuery(this.typePath)
      .select(this.idFieldName, this.valueFieldName)
      .addOrderBy(this.orderBy);
  }

  @Override
  public synchronized void refresh() {
    this.clearCaches();
    super.refresh();
    if (isLoadAll()) {
      this.loaded = false;
      loadAll();
    }
  }

  @Override
  public void refreshIfNeeded() {
    if (this.loadAll) {
      super.refreshIfNeeded();
    }
  }

  public SingleValueRecordStoreCodeTable setAllowNullValues(final boolean allowNullValues) {
    this.allowNullValues = allowNullValues;
    return this;
  }

  public SingleValueRecordStoreCodeTable setCreateMissingCodes(final boolean createMissingCodes) {
    this.createMissingCodes = createMissingCodes;
    return this;
  }

  public SingleValueRecordStoreCodeTable setCreationTimestampFieldName(
    final String creationTimestampFieldName) {
    this.creationTimestampFieldName = creationTimestampFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setFieldAliases(final String... fieldNameAliases) {
    setFieldNameAliases(Lists.newArray(fieldNameAliases));
    return this;
  }

  public SingleValueRecordStoreCodeTable setFieldNameAliases(final List<String> fieldNameAliases) {
    this.fieldNameAliases = new ArrayList<>(fieldNameAliases);
    return this;
  }

  public SingleValueRecordStoreCodeTable setIdFieldName(final String idFieldName) {
    this.idFieldName = idFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setLoadAll(final boolean loadAll) {
    if (loadAll && !this.loadAll) {
      this.data = null;
    }
    this.loadAll = loadAll;
    return this;
  }

  @Override
  public SingleValueRecordStoreCodeTable setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
    return this;
  }

  public SingleValueRecordStoreCodeTable setModificationTimestampFieldName(
    final String modificationTimestampFieldName) {
    this.modificationTimestampFieldName = modificationTimestampFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setOrderByFieldName(final String orderByFieldName) {
    this.orderBy = orderByFieldName;
    return this;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
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
        final String name = this.typePath.getName();
        setName(name);
        if (this.idFieldName == null) {
          this.idFieldName = recordDefinition.getIdFieldName();
        }
        this.recordStore = this.recordDefinition.getRecordStore();
        setValueFieldDefinition(recordDefinition.getField(this.valueFieldName));
        setRecordDefinitionAfter(recordDefinition);
      }
    }
  }

  protected void setRecordDefinitionAfter(final RecordDefinition recordDefinition) {
  }

  protected void setRecordDefinitionBefore(final RecordDefinition oldRecordDefinition) {
  }

  public SingleValueRecordStoreCodeTable setValueFieldName(final String valueFieldName) {
    this.valueFieldName = valueFieldName;
    if (this.orderBy == DEFAULT_FIELD_NAME) {
      setOrderByFieldName(valueFieldName);
    }
    return this;
  }

  @Override
  public String toString() {
    return this.typePath + " " + getIdFieldName() + " " + this.valueFieldName;

  }

  public String toString(final List<String> values) {
    final StringBuilder string = new StringBuilder(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
