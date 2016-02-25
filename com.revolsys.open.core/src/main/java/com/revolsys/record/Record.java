package com.revolsys.record;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.MapDefault;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.identifier.Identifiable;
import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.ListIdentifier;
import com.revolsys.identifier.TypedIdentifier;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public interface Record
  extends MapDefault<String, Object>, Comparable<Record>, Identifiable, RecordDefinitionProxy {
  String EVENT_RECORD_CHANGED = "_recordChanged";

  String EXCLUDE_GEOMETRY = Record.class.getName() + ".excludeGeometry";

  String EXCLUDE_ID = Record.class.getName() + ".excludeId";

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static boolean equalsNotNull(final Object object1, final Object object2) {
    return ((Record)object1).equalValuesAll((Map)object2);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static boolean equalsNotNull(final Object object1, final Object object2,
    final Collection<String> excludeFieldNames) {
    return ((Record)object1).equalValuesAll((Map)object2, excludeFieldNames);
  }

  @SuppressWarnings("unchecked")
  default <R extends Record> int addTo(final List<R> records) {
    if (!contains(records)) {
      final int index = records.size();
      records.add((R)this);
      return index;
    }
    return -1;
  }

  Record clone();

  @Override
  default int compareTo(final Record other) {
    if (this == other) {
      return 0;
    } else {
      final int recordDefinitionCompare = getRecordDefinition()
        .compareTo(other.getRecordDefinition());
      if (recordDefinitionCompare == 0) {
        final Identifier id1 = getIdentifier();
        final Identifier id2 = other.getIdentifier();
        if (id1 == null) {
          if (id2 != null) {
            return -1;
          }
        } else {
          final int idCompare = id1.compareTo(id2);
          if (idCompare != 0) {
            return idCompare;
          }
        }
        final Geometry geometry1 = getGeometry();
        final Geometry geometry2 = other.getGeometry();
        if (geometry1 != null && geometry2 != null) {
          final int geometryComparison = geometry1.compareTo(geometry2);
          if (geometryComparison != 0) {
            return geometryComparison;
          }
        }
        final Integer hash1 = hashCode();
        final int hash2 = other.hashCode();
        final int hashCompare = hash1.compareTo(hash2);
        if (hashCompare != 0) {
          return hashCompare;
        }
        return -1;
      } else {
        return recordDefinitionCompare;
      }
    }
  }

  default boolean contains(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      if (isSame(record)) {
        return true;
      }
    }
    return false;
  }

  default void delete() {
    getRecordDefinition().deleteRecord(this);
  }

  default double distance(final Geometry geometry) {
    final Geometry recordGeometry = getGeometry();
    if (Property.isEmpty(geometry) || Property.isEmpty(recordGeometry)) {
      return Double.NaN;
    } else {
      final double distance = recordGeometry.distance(geometry);
      return distance;
    }
  }

  @Override
  default Set<Entry<String, Object>> entrySet() {
    return new RecordEntrySet(this);
  }

  default boolean equalPathValue(final CharSequence fieldPath, final Object value) {
    final Object fieldValue = getValueByPath(fieldPath);
    final boolean hasValue1 = Property.hasValue(value);
    final boolean hasValue2 = Property.hasValue(fieldValue);
    if (hasValue1) {
      if (hasValue2) {
        return DataType.equal(fieldValue, value);
      } else {
        return false;
      }
    } else {
      if (hasValue2) {
        return false;
      } else {
        return true;
      }
    }
  }

  default boolean equalValue(final CharSequence fieldName, final Object value) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return fieldDefinition.equals(fieldValue, value);
    }
  }

  default boolean equalValue(final CharSequence fieldName, final Object value,
    final CharSequence... excludeFieldNames) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return fieldDefinition.equals(fieldValue, value, excludeFieldNames);
    }
  }

  default boolean equalValue(final CharSequence fieldName, final Object value,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return fieldDefinition.equals(fieldValue, value, excludeFieldNames);
    }
  }

  default boolean equalValue(final Map<String, Object> map, final CharSequence fieldName) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return equalValue(fieldName, value);
    }
    return false;
  }

  default boolean equalValue(final Map<String, Object> map, final CharSequence fieldName,
    final CharSequence... excludeFieldNames) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return equalValue(fieldName, value, excludeFieldNames);
    }
    return false;
  }

  default boolean equalValue(final Map<String, Object> map, final CharSequence fieldName,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (isFieldExcluded(excludeFieldNames, fieldName)) {
      return true;
    } else {
      if (map != null) {
        final Object value = map.get(fieldName);
        return equalValue(fieldName, value, excludeFieldNames);
      }
      return false;
    }
  }

  default boolean equalValue(final Record otherRecord, final CharSequence fieldName,
    final CharSequence... excludeFieldNames) {
    if (otherRecord != null) {
      final Object value = otherRecord.getValue(fieldName);
      return equalValue(fieldName, value, excludeFieldNames);
    }
    return false;
  }

  /**
   * Equal if the map has all the fields and values of this record
   * @param map
   */
  default boolean equalValuesAll(final Map<String, Object> map) {
    if (map == null) {
      return false;
    } else {
      if (map instanceof Record) {
        final Record record = (Record)map;
        if (!record.getTypePath().equals(getTypePath())) {
          return false;
        }
      }
      final List<String> fieldNames = getFieldNames();
      for (final String fieldName : fieldNames) {
        if (!equalValue(map, fieldName)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Equal if the map has all the fields and values of this record
   * @param map
   */
  default boolean equalValuesAll(final Map<String, Object> map,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final List<String> fieldNames = getFieldNames();
    for (final String fieldName : fieldNames) {
      if (!equalValue(map, fieldName, excludeFieldNames)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default Object get(final Object key) {
    if (key instanceof CharSequence) {
      final CharSequence name = (String)key;
      return getValue(name);
    } else {
      return null;
    }
  }

  default Byte getByte(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.byteValue();
      } else {
        return Byte.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  default Double getDouble(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.doubleValue();
      } else {
        return Double.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  default <E extends Enum<E>> E getEnum(final Class<E> enumType, final CharSequence fieldName) {
    final String value = getString(fieldName);
    if (Property.hasValue(value)) {
      return Enum.valueOf(enumType, value);
    } else {
      return null;
    }
  }

  default Float getFloat(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.floatValue();
      } else {
        return Float.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  /**
   * Get the value of the primary geometry field.
   *
   * @return The primary geometry.
   */

  @SuppressWarnings("unchecked")
  default <T extends Geometry> T getGeometry() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final int index = recordDefinition.getGeometryFieldIndex();
      return (T)getValue(index);
    }
  }

  @Override
  default Identifier getIdentifier() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<Integer> idFieldIndexes = recordDefinition.getIdFieldIndexes();
    final int idCount = idFieldIndexes.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final Integer idFieldIndex = idFieldIndexes.get(0);
      final Object idValue = getValue(idFieldIndex);
      if (idValue == null) {
        return null;
      } else {
        return Identifier.newIdentifier(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final Integer idFieldIndex = idFieldIndexes.get(i);
        final Object value = getValue(idFieldIndex);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListIdentifier(idValues);
      } else {
        return null;
      }
    }
  }

  default Identifier getIdentifier(final CharSequence fieldName) {
    final Object value = getValue(fieldName);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final int index) {
    final Object value = getValue(index);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final List<? extends CharSequence> fieldNames) {
    final int idCount = fieldNames.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final CharSequence idFieldName = fieldNames.get(0);
      final Object idValue = getValue(idFieldName);
      if (idValue == null) {
        return null;
      } else {
        return Identifier.newIdentifier(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final CharSequence idFieldName = fieldNames.get(i);
        final Object value = getValue(idFieldName);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListIdentifier(idValues);
      } else {
        return null;
      }
    }
  }

  default Integer getInteger(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue();
      } else {
        return Integer.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  default int getInteger(final CharSequence name, final int defaultValue) {
    final Integer value = getInteger(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Long getLong(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.longValue();
      } else {
        return Long.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  /**
   * Get the meta data describing the record and it's fields.
   *
   * @return The meta data.
   */
  @Override
  RecordDefinition getRecordDefinition();

  default Short getShort(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.shortValue();
      } else {
        return Short.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  default RecordState getState() {
    return RecordState.NEW;
  }

  default String getString(final CharSequence fieldName) {
    final Object value = getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return value.toString();
    } else if (value instanceof Clob) {
      final Clob clob = (Clob)value;
      try {
        return clob.getSubString(1, (int)clob.length());
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else {
      return getFieldDefinition(fieldName).toString(value);
    }
  }

  default String getString(final CharSequence name, final String defaultValue) {
    final String value = getString(name);
    if (Property.hasValue(value)) {
      return value;
    } else {
      return defaultValue;
    }
  }

  default TypedIdentifier getTypedIdentifier(final String type) {
    final Identifier identifier = getIdentifier();
    return TypedIdentifier.newIdentifier(type, identifier);
  }

  /**
   * Get the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @return The field value.
   */

  @SuppressWarnings("unchecked")
  default <T extends Object> T getValue(final CharSequence name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    try {
      final int index = recordDefinition.getFieldIndex(name);
      return (T)getValue(index);
    } catch (final NullPointerException e) {
      LoggerFactory.getLogger(getClass())
        .warn("Field " + recordDefinition.getPath() + "." + name + " does not exist", e);
      return null;
    }
  }

  default <T extends Object> T getValue(final CharSequence name, final DataType dataType) {
    final Object value = getValue(name);
    return dataType.toObject(value);
  }

  /**
   * Get the value of the field with the specified index.
   *
   * @param index The index of the field.
   * @return The field value.
   */
  <T extends Object> T getValue(int index);

  default <T extends Object> T getValue(final int index, final DataType dataType) {
    final Object value = getValue(index);
    return dataType.toObject(value);
  }

  @SuppressWarnings("unchecked")
  default <T> T getValueByPath(final CharSequence path) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String[] propertyPath = path.toString().split("\\.");
    Object propertyValue = this;
    for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
      final String propertyName = propertyPath[i];
      if (propertyValue instanceof Record) {
        final Record record = (Record)propertyValue;

        if (record.hasField(propertyName)) {
          propertyValue = record.getValue(propertyName);
          if (propertyValue == null) {
            return null;
          } else if (i + 1 < propertyPath.length) {
            final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(propertyValue);
            }
          }
        } else {
          return null;
        }
      } else if (propertyValue instanceof Geometry) {
        final Geometry geometry = (Geometry)propertyValue;
        propertyValue = GeometryProperties.getGeometryProperty(geometry, propertyName);
      } else if (propertyValue instanceof Map) {
        final Map<String, Object> map = (Map<String, Object>)propertyValue;
        propertyValue = map.get(propertyName);
        if (propertyValue == null) {
          return null;
        } else if (i + 1 < propertyPath.length) {
          final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(propertyName);
          if (codeTable != null) {
            propertyValue = codeTable.getMap(propertyValue);
          }
        }
      } else {
        try {
          propertyValue = JavaBeanUtil.getProperty(propertyValue, propertyName);
        } catch (final IllegalArgumentException e) {
          LoggerFactory.getLogger(getClass()).debug("Path does not exist " + path, e);
          return null;
        }
      }
    }
    return (T)propertyValue;
  }

  default Map<String, Object> getValueMap(final Collection<? extends CharSequence> fieldNames) {
    final Map<String, Object> values = new HashMap<>();
    for (final CharSequence name : fieldNames) {
      final Object value = getValue(name);
      if (value != null) {
        values.put(name.toString(), value);
      }
    }
    return values;
  }

  default List<Object> getValues() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<Object> values = new ArrayList<>();
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      final Object value = getValue(i);
      values.add(value);
    }
    return values;
  }

  default boolean hasGeometry() {
    final Geometry geometry = getGeometry();
    return Property.hasValue(geometry);
  }

  default boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return Property.hasValue(value);
  }

  default boolean hasValuesAll(final CharSequence... fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      if (!hasValue(fieldName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if any of the fields have a value.
   *
   * @param fieldNames
   * @return True if any of the fields have a value, false otherwise.
   */
  default boolean hasValuesAny(final CharSequence... fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      if (hasValue(fieldName)) {
        return true;
      }
    }
    return false;
  }

  default int indexOf(final Iterable<? extends Record> records) {
    int index = 0;
    for (final Record record : records) {
      if (isSame(record)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  default boolean isChanged() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      return false;
    } else {
      return true;
    }
  }

  default boolean isDeleted() {
    final RecordState state = getState();
    if (state == RecordState.DELETED) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isFieldExcluded(final Collection<? extends CharSequence> excludeFieldNames,
    CharSequence fieldName) {
    fieldName = fieldName.toString();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (excludeFieldNames.contains(fieldName)) {
      return true;
    } else if (excludeFieldNames.contains(Record.EXCLUDE_ID)
      && ("OBJECTID".equals(fieldName) || recordDefinition.getIdFieldNames().contains(fieldName))) {
      return true;
    } else if (excludeFieldNames.contains(Record.EXCLUDE_GEOMETRY) && ("OBJECTID".equals(fieldName)
      || recordDefinition.getGeometryFieldNames().contains(fieldName))) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isModified() {
    final RecordState state = getState();
    if (state == RecordState.NEW) {
      return true;
    } else if (state == RecordState.MODIFIED) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isSame(final Record record) {
    if (record == null) {
      return false;
    } else {
      if (this == record) {
        return true;
      } else {
        synchronized (this) {
          if (record.getRecordDefinition() == getRecordDefinition()) {
            final Identifier id = getIdentifier();
            final Identifier otherId = record.getIdentifier();
            if (id == null || otherId == null) {
              return false;
            } else if (DataType.equal(id, otherId)) {
              return true;
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      }
    }
  }

  default boolean isValid(final CharSequence fieldName) {
    return true;
  }

  default boolean isValid(final int index) {
    return true;
  }

  @Override
  default Set<String> keySet() {
    return getRecordDefinition().getFieldNamesSet();
  }

  @Override
  default Object put(final String key, final Object value) {
    final Object oldValue = getValue(key);
    setValue(key, value);
    return oldValue;
  }

  @Override
  default void putAll(final Map<? extends String, ? extends Object> values) {
    setValues(values);
  }

  @Override
  default Object remove(final Object key) {
    if (key instanceof CharSequence) {
      final CharSequence name = (CharSequence)key;
      final Object value = getValue(name);
      setValue(name, null);
      return value;
    }
    return null;
  }

  /**
   * Remove the first record in the collection of records that is {{@link #isSame(Record)}} as this record.
   *
   * @param records
   * @return The index of the removed record.
   */
  default int removeFrom(final Iterable<? extends Record> records) {
    int index = 0;
    for (final Iterator<? extends Record> iterator = records.iterator(); iterator.hasNext();) {
      final Record record = iterator.next();
      if (record.isSame(this)) {
        iterator.remove();
        return index;
      } else {
        index++;
      }
    }
    return -1;
  }

  /**
   * Set the value of the primary geometry field.
   *
   * @param geometry The primary geometry.
   */

  default void setGeometryValue(final Geometry geometry) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getGeometryFieldIndex();
    setValue(index, geometry);
  }

  default void setIdentifier(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();

    final RecordState state = getState();
    if (state == RecordState.NEW || state == RecordState.INITIALIZING) {
      final List<String> idFieldNames = recordDefinition.getIdFieldNames();
      Identifier.setIdentifier(this, idFieldNames, identifier);
    } else {
      final Identifier oldIdentifier = getIdentifier();
      if (!DataTypes.IDENTIFIER.equals(oldIdentifier, identifier)) {
        throw new IllegalStateException(
          "Cannot change the ID on a persisted record: " + identifier + "!=" + oldIdentifier);
      }
    }
  }

  void setState(final RecordState state);

  /**
   * Set the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @param value The new value.
   */

  default boolean setValue(final CharSequence name, final Object value) {
    boolean updated = false;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getFieldIndex(name);
    if (index >= 0) {
      return setValue(index, value);
    } else {
      final int dotIndex = name.toString().indexOf('.');
      if (dotIndex == -1) {

      } else {
        final CharSequence key = name.subSequence(0, dotIndex);
        final CharSequence subKey = name.subSequence(dotIndex + 1, name.length());
        final Object recordValue = getValue(key);
        if (recordValue == null) {
          final DataType fieldType = recordDefinition.getFieldType(key);
          if (fieldType != null) {
            if (fieldType.getJavaClass() == Record.class) {
              final String typePath = fieldType.getName();
              final RecordDefinitionFactory recordDefinitionFactory = recordDefinition
                .getRecordDefinitionFactory();
              final RecordDefinition subRecordDefinition = recordDefinitionFactory
                .getRecordDefinition(typePath);
              final RecordFactory recordFactory = subRecordDefinition.getRecordFactory();
              final Record subRecord = recordFactory.newRecord(subRecordDefinition);
              updated |= subRecord.setValue(subKey, value);
              updated |= setValue(key, subRecord);
            }
          }
        } else {
          if (recordValue instanceof Geometry) {
            final Geometry geometry = (Geometry)recordValue;
            GeometryProperties.setGeometryProperty(geometry, subKey, value);
            updated = true;
          } else if (recordValue instanceof Record) {
            final Record record = (Record)recordValue;
            updated |= record.setValue(subKey, value);
          } else {
            JavaBeanUtil.setProperty(recordValue, subKey.toString(), value);
            updated = true;
          }
        }
      }
    }
    return updated;
  }

  default <T> T setValue(final CharSequence fieldName, final Record source,
    final String sourceFieldName) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValue(sourceFieldName);
    setValueByPath(fieldName, value);
    return value;
  }

  /**
   * Set the value of the field with the specified name.
   *
   * @param index The index of the field.
   * @param value The new value;
   */
  boolean setValue(int index, Object value);

  @SuppressWarnings("rawtypes")
  default boolean setValueByPath(final CharSequence path, final Object value) {
    boolean updated = false;
    final String name = path.toString();
    final int dotIndex = name.indexOf(".");
    String codeTableFieldName;
    String codeTableValueName = null;
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (dotIndex == -1) {
      if (name.equals(recordDefinition.getIdFieldName())) {
        codeTableFieldName = null;
      } else {
        codeTableFieldName = name;
      }
    } else {
      codeTableFieldName = name.substring(0, dotIndex);
      codeTableValueName = name.substring(dotIndex + 1);
    }
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(codeTableFieldName);
    if (codeTable == null) {
      if (dotIndex != -1) {
        LoggerFactory.getLogger(getClass())
          .debug("Cannot get code table for " + recordDefinition.getPath() + "." + name);
        return false;
      }
      updated = setValue(name, value);
    } else if (!Property.hasValue(value)) {
      updated = setValue(codeTableFieldName, null);
    } else {
      Object targetValue;
      if (codeTableValueName == null) {
        Identifier id;
        if (value instanceof List) {
          final List list = (List)value;
          id = codeTable.getIdentifier(list.toArray());
        } else {
          id = codeTable.getIdentifier(value);
        }
        if (id == null) {
          targetValue = value;
        } else {
          targetValue = Value.getValue(id);
        }
      } else {
        targetValue = codeTable.getIdentifier(Collections.singletonMap(codeTableValueName, value));
      }
      if (targetValue == null) {
        targetValue = value;
      }
      updated = setValue(codeTableFieldName, targetValue);
    }
    return updated;
  }

  default <T> T setValueByPath(final CharSequence fieldPath, final Record source,
    final String sourceFieldPath) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValueByPath(sourceFieldPath);
    setValueByPath(fieldPath, value);
    return value;
  }

  default void setValues(final Map<? extends CharSequence, ? extends Object> values) {
    if (values instanceof Record) {
      final Record record = (Record)values;
      setValues(record);
    } else if (values != null) {
      setValues(values, new ArrayList<>(values.keySet()));
    }
  }

  default void setValues(final Map<? extends CharSequence, ? extends Object> values,
    final Collection<? extends CharSequence> fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      final Object newValue = values.get(fieldName);
      final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
      if (fieldDefinition != null) {
        fieldDefinition.setValue(this, newValue);
      }
    }
  }

  default void setValues(final Map<? extends String, ? extends Object> values,
    final String... fieldNames) {
    setValues(values, Arrays.asList(fieldNames));
  }

  void setValues(Object... values);

  default void setValues(final Record record) {
    if (record != null) {
      final List<FieldDefinition> idFields = getRecordDefinition().getIdFields();
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        if (!idFields.contains(fieldDefintion)) {
          final String name = fieldDefintion.getName();
          final Object value = record.getValue(name);
          fieldDefintion.setValue(this, value);
        }
      }
    }
  }

  default void setValuesByPath(final Map<? extends CharSequence, ? extends Object> values) {
    if (values != null) {
      for (final Entry<? extends CharSequence, ? extends Object> defaultValue : new ArrayList<>(
        values.entrySet())) {
        final CharSequence name = defaultValue.getKey();
        final Object value = defaultValue.getValue();
        setValueByPath(name, value);
      }
    }
  }

  default void setValuesClone(final Record record) {
    final List<FieldDefinition> fields = getFieldDefinitions();
    for (final FieldDefinition fieldDefintion : fields) {
      final String name = fieldDefintion.getName();
      final Object value = record.getValue(name);
      fieldDefintion.setValueClone(this, value);
    }
  }

  @Override
  default int size() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldCount();
  }

  default void validateField(final int fieldIndex) {
    final FieldDefinition field = getFieldDefinition(fieldIndex);
    if (field != null) {
      final Object value = getValue(fieldIndex);
      field.validate(this, value);
    }
  }
}
