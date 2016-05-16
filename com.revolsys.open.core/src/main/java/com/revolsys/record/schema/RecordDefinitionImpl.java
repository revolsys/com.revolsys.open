package com.revolsys.record.schema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.collection.map.WeakKeyValueMap;
import com.revolsys.collection.set.Sets;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.code.CodeTableProperty;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.property.ValueRecordDefinitionProperty;

public class RecordDefinitionImpl extends AbstractRecordStoreSchemaElement
  implements RecordDefinition {
  private static final AtomicInteger INSTANCE_IDS = new AtomicInteger(0);

  private static final Map<Integer, RecordDefinitionImpl> RECORD_DEFINITION_CACHE = new WeakKeyValueMap<Integer, RecordDefinitionImpl>();

  public static void destroy(final RecordDefinitionImpl... recordDefinitionList) {
    for (final RecordDefinitionImpl recordDefinition : recordDefinitionList) {
      recordDefinition.destroy();
    }
  }

  public static RecordDefinition getRecordDefinition(final int instanceId) {
    return RECORD_DEFINITION_CACHE.get(instanceId);
  }

  public static RecordDefinitionImpl newRecordDefinition(final Map<String, Object> properties) {
    return new RecordDefinitionImpl(properties);
  }

  private Map<String, CodeTable> codeTableByFieldNameMap = new HashMap<>();

  private Map<String, Object> defaultValues = new HashMap<>();

  private String description;

  private final Map<String, Integer> fieldIdMap = new HashMap<>();

  private final Map<String, FieldDefinition> fieldMap = new HashMap<>();

  private List<String> fieldNames = Collections.emptyList();

  private Set<String> fieldNamesSet = Collections.emptySet();

  private List<FieldDefinition> fields = Collections.emptyList();

  /** The index of the primary geometry field. */
  private int geometryFieldDefinitionIndex = -1;

  private final List<Integer> geometryFieldDefinitionIndexes = new ArrayList<>();

  private final List<String> geometryFieldDefinitionNames = new ArrayList<>();

  /** The index of the ID field. */
  private int idFieldDefinitionIndex = -1;

  private final List<Integer> idFieldDefinitionIndexes = new ArrayList<>();

  private final List<String> idFieldDefinitionNames = new ArrayList<>();

  private final List<FieldDefinition> idFieldDefinitions = new ArrayList<>();

  private final Integer instanceId = INSTANCE_IDS.getAndIncrement();

  private final List<String> internalFieldNames = new ArrayList<>();

  private final List<FieldDefinition> internalFields = new ArrayList<>();

  private RecordDefinitionFactory recordDefinitionFactory;

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  private RecordFactory<Record> recordFactory = (RecordFactory)ArrayRecord.FACTORY;

  private final Map<String, Collection<Object>> restrictions = new HashMap<>();

  private final List<RecordDefinition> superClasses = new ArrayList<>();

  public RecordDefinitionImpl() {
    super(null, (PathName)null);
  }

  @SuppressWarnings("unchecked")
  public RecordDefinitionImpl(final Map<String, Object> properties) {
    this(PathName.newPathName(Maps.getString(properties, "path")));
    final List<Object> fields = (List<Object>)properties.get("fields");
    for (final Object object : fields) {
      if (object instanceof FieldDefinition) {
        final FieldDefinition field = (FieldDefinition)object;
        addField(field.clone());
      } else if (object instanceof Map) {
        final Map<String, Object> fieldProperties = (Map<String, Object>)object;
        final FieldDefinition field = FieldDefinition.newFieldDefinition(fieldProperties);
        addField(field);
      }
    }
    final Map<String, Object> geometryFactoryDef = (Map<String, Object>)properties
      .get("geometryFactory");
    if (geometryFactoryDef != null) {
      final GeometryFactory geometryFactory = MapObjectFactory.toObject(geometryFactoryDef);
      setGeometryFactory(geometryFactory);
    }
  }

  public RecordDefinitionImpl(final PathName path) {
    super(path);
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final PathName path, final FieldDefinition... fields) {
    this(path, null, fields);
  }

  public RecordDefinitionImpl(final PathName path, final List<FieldDefinition> fields) {
    this(path, null, fields);
  }

  public RecordDefinitionImpl(final PathName path, final Map<String, Object> properties,
    final FieldDefinition... fields) {
    this(path, properties, Arrays.asList(fields));
  }

  public RecordDefinitionImpl(final PathName path, final Map<String, Object> properties,
    final List<FieldDefinition> fields) {
    super(path);
    for (final FieldDefinition field : fields) {
      addField(field.clone());
    }
    cloneProperties(properties);
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final RecordDefinition recordDefinition) {
    this(recordDefinition.getPathName(), recordDefinition.getProperties(),
      recordDefinition.getFields());
    setIdFieldIndex(recordDefinition.getIdFieldIndex());
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema, final PathName pathName) {
    super(schema, pathName);
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema, final PathName path,
    final Map<String, Object> properties, final List<FieldDefinition> fields) {
    this(schema, path);
    for (final FieldDefinition field : fields) {
      addField(field.clone());
    }
    cloneProperties(properties);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema,
    final RecordDefinition recordDefinition) {
    this(schema, recordDefinition.getPathName());
    for (final FieldDefinition field : recordDefinition.getFields()) {
      addField(field.clone());
    }
    cloneProperties(recordDefinition.getProperties());
  }

  @Override
  public void addDefaultValue(final String fieldName, final Object defaultValue) {
    this.defaultValues.put(fieldName, defaultValue);
  }

  public synchronized void addField(final FieldDefinition field) {
    final int index = this.fieldNames.size();
    final String name = field.getName();
    String lowerName;
    if (name == null) {
      lowerName = null;
    } else {
      lowerName = name.toLowerCase();
    }

    this.internalFieldNames.add(name);
    this.fieldNames = Lists.unmodifiable(this.internalFieldNames);
    this.fieldNamesSet = Sets.unmodifiableLinked(this.internalFieldNames);
    this.internalFields.add(field);
    this.fields = Lists.unmodifiable(this.internalFields);
    this.fieldMap.put(lowerName, field);
    this.fieldIdMap.put(lowerName, this.fieldIdMap.size());
    final DataType dataType = field.getDataType();
    if (dataType == null) {
      Logs.debug(this, field.toString());
    } else {
      final Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        this.geometryFieldDefinitionIndexes.add(index);
        this.geometryFieldDefinitionNames.add(name);
        if (this.geometryFieldDefinitionIndex == -1) {
          this.geometryFieldDefinitionIndex = index;
        }
      }
    }
    field.setIndex(index);
    field.setRecordDefinition(this);
  }

  /**
   * Adds an field with the given case-sensitive name.
   *
   */
  public FieldDefinition addField(final String fieldName, final DataType type) {
    return addField(fieldName, type, false);
  }

  public FieldDefinition addField(final String name, final DataType type, final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, required);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String name, final DataType type, final int length,
    final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, length, required);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String fieldName, final DataType type, final int length,
    final int scale) {
    final FieldDefinition field = new FieldDefinition(fieldName, type, length, scale, false);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String name, final DataType type, final int length,
    final int scale, final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, length, scale, required);
    addField(field);
    return field;
  }

  public void addFieldCodeTable(final String fieldName, final CodeTable codeTable) {
    this.codeTableByFieldNameMap.put(fieldName, codeTable);
  }

  public void addRestriction(final String fieldPath, final Collection<Object> values) {
    this.restrictions.put(fieldPath, values);
  }

  public void addSuperClass(final RecordDefinition superClass) {
    if (!this.superClasses.contains(superClass)) {
      this.superClasses.add(superClass);
    }
  }

  public void cloneProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        final Object value = property.getValue();
        if (value instanceof RecordDefinitionProperty) {
          RecordDefinitionProperty recordDefinitionProperty = (RecordDefinitionProperty)value;
          recordDefinitionProperty = recordDefinitionProperty.clone();
          recordDefinitionProperty.setRecordDefinition(this);
          setProperty(propertyName, recordDefinitionProperty);
        } else {
          setProperty(propertyName, value);
        }
      }
    }
  }

  @Override
  public void deleteRecord(final Record record) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new UnsupportedOperationException();
    } else {
      recordStore.deleteRecord(record);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.close();
    RECORD_DEFINITION_CACHE.remove(this.instanceId);
    this.fieldIdMap.clear();
    this.fieldMap.clear();
    this.internalFieldNames.clear();
    this.fields = Collections.emptyList();
    this.internalFields.clear();
    this.fieldNames = Collections.emptyList();
    this.fieldNamesSet = Collections.emptySet();
    this.codeTableByFieldNameMap.clear();
    this.recordFactory = null;
    this.recordDefinitionFactory = new RecordDefinitionFactoryImpl();
    this.defaultValues.clear();
    this.description = "";
    this.geometryFieldDefinitionIndex = -1;
    this.geometryFieldDefinitionIndexes.clear();
    this.geometryFieldDefinitionNames.clear();
    this.restrictions.clear();
    this.superClasses.clear();
  }

  @Override
  public CodeTable getCodeTableByFieldName(final String fieldName) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      return null;
    } else {
      CodeTable codeTable;
      final FieldDefinition fieldDefinition = getField(fieldName);
      if (fieldDefinition != null) {
        codeTable = fieldDefinition.getCodeTable();
        if (codeTable != null) {
          return codeTable;
        }
      }
      codeTable = this.codeTableByFieldNameMap.get(fieldName);
      if (codeTable == null && recordStore != null) {
        codeTable = recordStore.getCodeTableByFieldName(fieldName);
      }
      if (codeTable instanceof CodeTableProperty) {
        @SuppressWarnings("resource")
        final CodeTableProperty property = (CodeTableProperty)codeTable;
        if (property.getRecordDefinition() == this) {
          return null;
        }
      }
      if (fieldDefinition != null && codeTable != null) {
        fieldDefinition.setCodeTable(codeTable);
      }
      return codeTable;
    }
  }

  @Override
  public Object getDefaultValue(final String fieldName) {
    return this.defaultValues.get(fieldName);
  }

  @Override
  public Map<String, Object> getDefaultValues() {
    return this.defaultValues;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public FieldDefinition getField(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      final String lowerName = name.toString().toLowerCase();
      return this.fieldMap.get(lowerName);
    }
  }

  @Override
  public FieldDefinition getField(final int i) {
    return this.fields.get(i);
  }

  @Override
  public Class<?> getFieldClass(final CharSequence name) {
    final DataType dataType = getFieldType(name);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public Class<?> getFieldClass(final int i) {
    final DataType dataType = getFieldType(i);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public int getFieldCount() {
    return this.fields.size();
  }

  @Override
  public int getFieldIndex(final CharSequence name) {
    if (name == null) {
      return -1;
    } else {
      final String lowerName = name.toString().toLowerCase();
      final Integer fieldId = this.fieldIdMap.get(lowerName);
      if (fieldId == null) {
        return -1;
      } else {
        return fieldId;
      }
    }
  }

  @Override
  public int getFieldLength(final int i) {
    try {
      final FieldDefinition field = this.fields.get(i);
      return field.getLength();
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public String getFieldName(final int i) {
    if (this.fields != null && i >= 0 && i < this.fields.size()) {
      final FieldDefinition field = this.fields.get(i);
      return field.getName();
    }
    return null;
  }

  @Override
  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public Set<String> getFieldNamesSet() {
    return this.fieldNamesSet;
  }

  @Override
  public List<FieldDefinition> getFields() {
    return this.fields;
  }

  @Override
  public int getFieldScale(final int i) {
    final FieldDefinition field = this.fields.get(i);
    return field.getScale();
  }

  @Override
  public List<String> getFieldTitles() {
    final List<String> titles = new ArrayList<String>();
    for (final FieldDefinition field : getFields()) {
      titles.add(field.getTitle());
    }
    return titles;
  }

  @Override
  public DataType getFieldType(final CharSequence name) {
    final int index = getFieldIndex(name);
    if (index == -1) {
      return null;
    } else {
      return getFieldType(index);
    }
  }

  @Override
  public DataType getFieldType(final int i) {
    final FieldDefinition field = this.fields.get(i);
    return field.getDataType();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    final FieldDefinition geometryFieldDefinition = getGeometryField();
    if (geometryFieldDefinition == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometryFieldDefinition
        .getProperty(FieldProperties.GEOMETRY_FACTORY);
      return geometryFactory;
    }
  }

  @Override
  public FieldDefinition getGeometryField() {
    if (this.geometryFieldDefinitionIndex == -1) {
      return null;
    } else {
      return this.fields.get(this.geometryFieldDefinitionIndex);
    }
  }

  @Override
  public int getGeometryFieldIndex() {
    return this.geometryFieldDefinitionIndex;
  }

  @Override
  public List<Integer> getGeometryFieldIndexes() {
    return Collections.unmodifiableList(this.geometryFieldDefinitionIndexes);
  }

  @Override
  public String getGeometryFieldName() {
    return getFieldName(this.geometryFieldDefinitionIndex);
  }

  @Override
  public List<String> getGeometryFieldNames() {
    return Collections.unmodifiableList(this.geometryFieldDefinitionNames);
  }

  @Override
  public String getIconName() {
    final FieldDefinition geometryField = getGeometryField();
    if (geometryField == null) {
      return "table";
    } else {
      final DataType dataType = geometryField.getDataType();
      if (dataType.equals(DataTypes.GEOMETRY_COLLECTION)) {
        return "table_geometry";
      } else {
        return "table_" + dataType.toString().toLowerCase();
      }
    }
  }

  @Override
  public FieldDefinition getIdField() {
    if (this.idFieldDefinitionIndex >= 0) {
      return this.fields.get(this.idFieldDefinitionIndex);
    } else {
      return null;
    }
  }

  @Override
  public int getIdFieldIndex() {
    return this.idFieldDefinitionIndex;
  }

  @Override
  public List<Integer> getIdFieldIndexes() {
    return Collections.unmodifiableList(this.idFieldDefinitionIndexes);
  }

  @Override
  public String getIdFieldName() {
    return getFieldName(this.idFieldDefinitionIndex);
  }

  @Override
  public List<String> getIdFieldNames() {
    return Collections.unmodifiableList(this.idFieldDefinitionNames);
  }

  @Override
  public List<FieldDefinition> getIdFields() {
    return Collections.unmodifiableList(this.idFieldDefinitions);
  }

  @Override
  public int getInstanceId() {
    return this.instanceId;
  }

  @Override
  public RecordDefinitionFactory getRecordDefinitionFactory() {
    if (this.recordDefinitionFactory == null) {
      final RecordStore recordStore = getRecordStore();
      return recordStore;
    } else {
      return this.recordDefinitionFactory;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> RecordFactory<R> getRecordFactory() {
    return (RecordFactory<R>)this.recordFactory;
  }

  public Map<String, Collection<Object>> getRestrictions() {
    return this.restrictions;
  }

  @Override
  public boolean hasField(final CharSequence name) {
    if (name == null) {
      return false;
    } else {
      final String lowerName = name.toString().toLowerCase();
      return this.fieldMap.containsKey(lowerName);
    }
  }

  @Override
  public boolean hasGeometryField() {
    return this.geometryFieldDefinitionIndex != -1;
  }

  @Override
  public boolean isFieldRequired(final CharSequence name) {
    final FieldDefinition field = getField(name);
    if (field == null) {
      return false;
    } else {
      return field.isRequired();
    }
  }

  @Override
  public boolean isFieldRequired(final int i) {
    final FieldDefinition field = getField(i);
    return field.isRequired();
  }

  @Override
  public boolean isInstanceOf(final RecordDefinition classDefinition) {
    if (classDefinition == null) {
      return false;
    }
    if (equals(classDefinition)) {
      return true;
    }
    for (final RecordDefinition superClass : this.superClasses) {
      if (superClass.isInstanceOf(classDefinition)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Record newRecord() {
    final RecordFactory<Record> recordFactory = this.recordFactory;
    if (recordFactory == null) {
      return new ArrayRecord(this);
    } else {
      return recordFactory.newRecord(this);
    }
  }

  private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl rename(final String path) {
    final RecordDefinitionImpl clone = new RecordDefinitionImpl(PathName.newPathName(path),
      getProperties(), this.fields);
    clone.setIdFieldIndex(this.idFieldDefinitionIndex);
    clone.setProperties(getProperties());
    return clone;
  }

  public void replaceField(final FieldDefinition field, final FieldDefinition newFieldDefinition) {
    final String name = field.getName();
    final String lowerName = name.toLowerCase();
    final String newName = newFieldDefinition.getName();
    if (this.fields.contains(field) && name.equals(newName)) {
      final int index = field.getIndex();
      this.internalFields.set(index, newFieldDefinition);
      this.fields = Lists.unmodifiable(this.internalFields);
      this.fieldMap.put(lowerName, newFieldDefinition);
      newFieldDefinition.setIndex(index);
    } else {
      addField(newFieldDefinition);
    }
  }

  public void setCodeTableByFieldNameMap(final Map<String, CodeTable> codeTableByFieldNameMap) {
    this.codeTableByFieldNameMap = codeTableByFieldNameMap;
  }

  @Override
  public void setDefaultValues(final Map<String, ? extends Object> defaultValues) {
    this.defaultValues = Maps.newHash(defaultValues);
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final FieldDefinition geometryFieldDefinition = getGeometryField();
    if (geometryFieldDefinition != null) {
      geometryFieldDefinition.setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
    }
  }

  /**
   * @param geometryFieldDefinitionIndex the geometryFieldDefinitionIndex to set
   */
  public void setGeometryFieldIndex(final int geometryFieldDefinitionIndex) {
    this.geometryFieldDefinitionIndex = geometryFieldDefinitionIndex;
  }

  public void setGeometryFieldName(final String name) {
    final int id = getFieldIndex(name);
    setGeometryFieldIndex(id);
  }

  /**
   * @param idFieldDefinitionIndex the idFieldDefinitionIndex to set
   */
  public void setIdFieldIndex(final int idFieldDefinitionIndex) {
    this.idFieldDefinitionIndex = idFieldDefinitionIndex;
    this.idFieldDefinitionIndexes.clear();
    this.idFieldDefinitionIndexes.add(idFieldDefinitionIndex);
    this.idFieldDefinitionNames.clear();
    this.idFieldDefinitionNames.add(getIdFieldName());
    this.idFieldDefinitions.clear();
    this.idFieldDefinitions.add(getIdField());
  }

  public void setIdFieldName(final String name) {
    final int id = getFieldIndex(name);
    setIdFieldIndex(id);
  }

  public void setIdFieldNames(final Collection<String> names) {
    if (names != null) {
      if (names.size() == 1) {
        final String name = CollectionUtil.get(names, 0);
        setIdFieldName(name);
      } else {
        for (final String name : names) {
          final int index = getFieldIndex(name);
          if (index == -1) {
            LoggerFactory.getLogger(getClass())
              .error("Cannot set ID " + getPath() + "." + name + " does not exist");
          } else {
            this.idFieldDefinitionIndexes.add(index);
            this.idFieldDefinitionNames.add(name);
            this.idFieldDefinitions.add(getField(index));
          }
        }
      }
    }
  }

  public void setIdFieldNames(final String... names) {
    setIdFieldNames(Arrays.asList(names));
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value instanceof ValueRecordDefinitionProperty) {
          final ValueRecordDefinitionProperty valueProperty = (ValueRecordDefinitionProperty)value;
          final String propertyName = valueProperty.getPropertyName();
          final Object propertyValue = valueProperty.getValue();
          setProperty(propertyName, propertyValue);
        }
        if (value instanceof RecordDefinitionProperty) {
          final RecordDefinitionProperty property = (RecordDefinitionProperty)value;
          final RecordDefinitionProperty clonedProperty = property.clone();
          clonedProperty.setRecordDefinition(this);
        } else {
          setProperty(key, value);
        }
      }
    }

  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, "recordDefinition");
    final String path = getPath();
    map.put("path", path);
    final GeometryFactory geometryFactory = getGeometryFactory();
    addToMap(map, "geometryFactory", geometryFactory, null);
    final List<FieldDefinition> fields = getFields();
    addToMap(map, "fields", fields);
    return map;
  }

}
