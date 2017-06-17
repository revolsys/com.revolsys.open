package com.revolsys.record.code;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.revolsys.collection.list.Lists;
import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.ListIdentifier;
import com.revolsys.identifier.SingleIdentifier;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.comparator.RecordFieldComparator;
import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

public class CodeTableProperty extends AbstractCodeTable implements RecordDefinitionProperty {

  private static final List<String> DEFAULT_FIELD_NAMES = Lists.newArray("VALUE");

  public static final String PROPERTY_NAME = CodeTableProperty.class.getName();

  public static final CodeTableProperty getProperty(final RecordDefinition recordDefinition) {
    final CodeTableProperty property = recordDefinition.getProperty(PROPERTY_NAME);
    return property;
  }

  private boolean createMissingCodes = true;

  private String creationTimestampFieldName;

  private List<String> fieldAliases = new ArrayList<>();

  private String idFieldName;

  private boolean loadAll = true;

  private boolean loaded = false;

  private boolean loading = false;

  private boolean loadMissingCodes = true;

  private String modificationTimestampFieldName;

  private List<String> orderBy = DEFAULT_FIELD_NAMES;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  private final ThreadLocal<Boolean> threadLoading = new ThreadLocal<>();

  private PathName typePath;

  private List<String> valueFieldNames = DEFAULT_FIELD_NAMES;

  private FieldDefinition valueFieldDefinition;

  private boolean allowNullValues = false;

  public CodeTableProperty() {
  }

  public CodeTableProperty(final Map<String, ? extends Object> config) {
    setProperties(config);
  }

  public void addFieldAlias(final String columnName) {
    this.fieldAliases.add(columnName);
  }

  public void addValue(final Record code) {
    final String idFieldName = getIdFieldName();
    final Identifier id = code.getIdentifier(idFieldName);
    if (id == null) {
      throw new NullPointerException(idFieldName + "=null for " + code);
    } else {
      final List<Object> values = new ArrayList<>();
      for (final String fieldName : this.valueFieldNames) {
        Object value = code.getValue(fieldName);
        if (value instanceof SingleIdentifier) {
          final SingleIdentifier identifier = (SingleIdentifier)value;
          value = identifier.getValue(0);
        }
        if (value == null) {
          if (!this.allowNullValues) {
            throw new NullPointerException(this.valueFieldNames + "=null for " + code);
          }
        }
        values.add(value);
      }
      addValue(id, values);
    }
  }

  protected void addValues(final Iterable<Record> allCodes) {
    for (final Record code : allCodes) {
      addValue(code);
    }
  }

  @Override
  public CodeTableProperty clone() {
    final CodeTableProperty clone = (CodeTableProperty)super.clone();
    clone.recordDefinition = null;
    clone.fieldAliases = new ArrayList<>(this.fieldAliases);
    clone.valueFieldNames = new ArrayList<>(this.valueFieldNames);
    return clone;
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    refreshIfNeeded();
    final Map<Identifier, List<Object>> codes = super.getCodes();
    return codes;
  }

  public String getCreationTimestampFieldName() {
    return this.creationTimestampFieldName;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.fieldAliases;
  }

  @Override
  public String getIdFieldName() {
    if (Property.hasValue(this.idFieldName)) {
      return this.idFieldName;
    } else if (this.recordDefinition == null) {
      return "";
    } else {
      final String idFieldName = this.recordDefinition.getIdFieldName();
      if (Property.hasValue(idFieldName)) {
        return idFieldName;
      } else {
        return this.recordDefinition.getFieldName(0);
      }
    }
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<>();
      for (int i = 0; i < values.size(); i++) {
        final String name = this.valueFieldNames.get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  public String getModificationTimestampFieldName() {
    return this.modificationTimestampFieldName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public Record getRecord(final Identifier id) {
    return this.recordStore.getRecord(this.typePath, id);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  public String getTypeName() {
    return this.typePath.getPath();
  }

  public PathName getTypePath() {
    return this.typePath;
  }

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

  @Override
  public FieldDefinition getValueFieldDefinition() {
    return this.valueFieldDefinition;
  }

  @Override
  public List<String> getValueFieldNames() {
    return this.valueFieldNames;
  }

  public boolean isAllowNullValues() {
    return this.allowNullValues;
  }

  public boolean isCreateMissingCodes() {
    return this.createMissingCodes;
  }

  public boolean isLoadAll() {
    return this.loadAll;
  }

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public boolean isLoading() {
    return this.loading;
  }

  public synchronized void loadAll() {
    if (this.threadLoading.get() != Boolean.TRUE) {
      if (this.loading) {
        while (this.loading) {
          try {
            wait(1000);
          } catch (final InterruptedException e) {
          }
        }
        return;
      } else {
        this.threadLoading.set(Boolean.TRUE);
        this.loading = true;
        try {
          final RecordDefinition recordDefinition = this.recordStore
            .getRecordDefinition(this.typePath);
          final Query query = new Query(recordDefinition);
          query.setFieldNames(recordDefinition.getFieldNames());
          for (final String order : this.orderBy) {
            query.addOrderBy(order);
          }
          try (
            Reader<Record> reader = this.recordStore.getRecords(query)) {
            final List<Record> codes = reader.toList();
            this.recordStore.getStatistics().getLabelCountMap("query").addCount(this.typePath,
              -codes.size());
            Collections.sort(codes, new RecordFieldComparator(this.orderBy));
            addValues(codes);
          }
          Property.firePropertyChange(this, "valuesChanged", false, true);
        } finally {
          this.loading = false;
          this.loaded = true;
          this.threadLoading.set(null);
          this.notifyAll();
        }
      }
    }
  }

  @Override
  protected synchronized Identifier loadId(final List<Object> values, final boolean createId) {
    if (this.loadAll && !this.loadMissingCodes && !isEmpty()) {
      return null;
    }
    Identifier id = null;
    if (createId && this.loadAll && !isLoaded()) {
      loadAll();
      id = getIdentifier(values, false);
    } else {
      final Query query = new Query(this.typePath);
      final And and = new And();
      if (!values.isEmpty()) {
        int i = 0;
        for (final String fieldName : this.valueFieldNames) {
          final Object value = values.get(i);
          if (value == null) {
            and.and(Q.isNull(fieldName));
          } else {
            final FieldDefinition fieldDefinition = this.recordDefinition.getField(fieldName);
            and.and(Q.equal(fieldDefinition, value));
          }
          i++;
        }
      }
      query.setWhereCondition(and);
      final Reader<Record> reader = this.recordStore.getRecords(query);
      try {
        final List<Record> codes = reader.toList();
        if (codes.size() > 0) {
          this.recordStore.getStatistics().getLabelCountMap("query").addCount(this.typePath,
            -codes.size());

          addValues(codes);
        }
        id = getIdByValue(values);
        Property.firePropertyChange(this, "valuesChanged", false, true);
      } finally {
        reader.close();
      }
    }
    if (createId && id == null) {
      return newIdentifier(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    if (this.loadAll && !isLoaded()) {
      loadAll();
    } else {
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
        return null;
      }
    }
    return getValueById(id);
  }

  protected synchronized Identifier newIdentifier(final List<Object> values) {
    if (this.createMissingCodes) {
      // TODO prevent duplicates from other threads/processes
      final Record code = this.recordStore.newRecord(this.typePath);
      final RecordDefinition recordDefinition = code.getRecordDefinition();
      Identifier id = this.recordStore.newPrimaryIdentifier(this.typePath);
      if (id == null) {
        final FieldDefinition idField = recordDefinition.getIdField();
        if (idField != null) {
          if (Number.class.isAssignableFrom(idField.getDataType().getJavaClass())) {
            id = Identifier.newIdentifier(getNextId());
          } else {
            id = Identifier.newIdentifier(UUID.randomUUID().toString());
          }
        }
      }
      code.setIdentifier(id);
      for (int i = 0; i < this.valueFieldNames.size(); i++) {
        final String name = this.valueFieldNames.get(i);
        final Object value = values.get(i);
        code.setValue(name, value);
      }

      final Timestamp now = new Timestamp(System.currentTimeMillis());
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
  public synchronized void refresh() {
    super.refresh();
    if (isLoadAll()) {
      this.loaded = false;
      loadAll();
    }
  }

  public void setAllowNullValues(final boolean allowNullValues) {
    this.allowNullValues = allowNullValues;
  }

  public void setCreateMissingCodes(final boolean createMissingCodes) {
    this.createMissingCodes = createMissingCodes;
  }

  public void setCreationTimestampFieldName(final String creationTimestampFieldName) {
    this.creationTimestampFieldName = creationTimestampFieldName;
  }

  public void setFieldAliases(final List<String> fieldAliases) {
    this.fieldAliases = new ArrayList<>(fieldAliases);
  }

  public void setFieldAliases(final String... fieldAliases) {
    setFieldAliases(Lists.newArray(fieldAliases));
  }

  public void setIdFieldName(final String idFieldName) {
    this.idFieldName = idFieldName;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
  }

  public void setModificationTimestampFieldName(final String modificationTimestampFieldName) {
    this.modificationTimestampFieldName = modificationTimestampFieldName;
  }

  public void setOrderBy(final List<String> orderBy) {
    this.orderBy = new ArrayList<>(orderBy);
  }

  public void setOrderByFieldName(final String orderBy) {
    this.orderBy = Lists.newArray(orderBy);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (this.recordDefinition != recordDefinition) {
      if (this.recordDefinition != null) {
        this.recordDefinition.setProperty(getPropertyName(), null);
        this.valueFieldDefinition = null;
      }
      this.recordDefinition = recordDefinition;
      if (recordDefinition == null) {
        this.recordStore = null;
        this.typePath = null;
      } else {
        this.typePath = recordDefinition.getPathName();
        final String name = this.typePath.getName();
        setName(name);
        this.recordStore = this.recordDefinition.getRecordStore();
        recordDefinition.setProperty(getPropertyName(), this);
        this.recordStore.addCodeTable(this);
        if (!this.valueFieldNames.isEmpty()) {
          final String fieldName = this.valueFieldNames.get(0);
          this.valueFieldDefinition = recordDefinition.getField(fieldName);
        }
      }
    }
  }

  public void setValueFieldName(final String valueColumns) {
    setValueFieldNames(valueColumns);
  }

  public void setValueFieldNames(final List<String> valueColumns) {
    this.valueFieldNames = new ArrayList<>(valueColumns);
    if (this.orderBy == DEFAULT_FIELD_NAMES) {
      setOrderBy(valueColumns);
    }
  }

  public void setValueFieldNames(final String... valueColumns) {
    setValueFieldNames(Arrays.asList(valueColumns));
  }

  @Override
  public String toString() {
    return this.typePath + " " + getIdFieldName() + " " + this.valueFieldNames;

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
