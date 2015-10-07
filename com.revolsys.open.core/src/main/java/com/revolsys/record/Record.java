package com.revolsys.record;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.equals.Equals;
import com.revolsys.equals.EqualsInstance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.identifier.Identifiable;
import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.ListIdentifier;
import com.revolsys.identifier.TypedIdentifier;
import com.revolsys.io.PathName;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public interface Record extends Map<String, Object>, Comparable<Record>, Identifiable {
  /**
   * Construct a new clone of the data record.
   *
   * @return The data record.
   */
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

  default void delete() {
    getRecordDefinition().delete(this);
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
        return Equals.equal(fieldValue, value);
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
    final Object fieldValue = getValue(fieldName);
    final boolean hasValue1 = Property.hasValue(value);
    final boolean hasValue2 = Property.hasValue(fieldValue);
    if (hasValue1) {
      if (hasValue2) {
        return Equals.equal(fieldValue, value);
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

  default boolean equalValue(final Record otherRecord, final CharSequence fieldName) {
    if (otherRecord != null) {
      final Object value = getValue(fieldName);
      return otherRecord.equalValue(fieldName, value);
    }
    return false;
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

  /**
   * Get the factory which created the instance.
   *
   * @return The factory.
   */

  default RecordFactory getFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordFactory();
    }
  }

  default FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldIndex);
  }

  default String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldTitle(name);
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
        return Identifier.create(idValue);
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

  default Identifier getIdentifier(final List<String> fieldNames) {
    final int idCount = fieldNames.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final String idFieldName = fieldNames.get(0);
      final Object idValue = getValue(idFieldName);
      if (idValue == null) {
        return null;
      } else {
        return Identifier.create(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final String idFieldName = fieldNames.get(i);
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

  default Identifier getIdentifier(final String... fieldNames) {
    return getIdentifier(Arrays.asList(fieldNames));
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

  RecordState getState();

  default String getString(final CharSequence name) {
    final Object value = getValue(name);
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
      return StringConverterRegistry.toString(value);
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
    return TypedIdentifier.create(type, identifier);
  }

  default String getTypePath() {
    return getRecordDefinition().getPath();
  }

  default PathName getTypePathName() {
    return getRecordDefinition().getPathName();
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
        .warn("Field " + recordDefinition.getPath() + "." + name + " does not exist");
      return null;
    }
  }

  /**
   * Get the value of the field with the specified index.
   *
   * @param index The index of the field.
   * @return The field value.
   */
  <T extends Object> T getValue(int index);

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
          LoggerFactory.getLogger(getClass()).error("Path does not exist " + path, e);
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

  /**
   * Checks to see if the definition for this record has a field with the
   * specified name.
   *
   * @param name The name of the field.
   * @return True if the record has a field with the specified name.
   */
  default boolean hasField(final CharSequence name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.hasField(name);
  }

  default boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return Property.hasValue(value);
  }

  default boolean hasValuesAll(final String... fieldNames) {
    for (final String fieldName : fieldNames) {
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
  default boolean hasValuesAny(final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      if (hasValue(fieldName)) {
        return true;
      }
    }
    return false;
  }

  default boolean isModified() {
    if (getState() == RecordState.New) {
      return true;
    } else if (getState() == RecordState.Modified) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isValid(final int index) {
    return true;
  }

  default boolean isValid(final String fieldName) {
    return true;
  }

  @Override
  default Set<String> keySet() {
    return new LinkedHashSet<>(getRecordDefinition().getFieldNames());
  }

  @Override
  default Object put(final String key, final Object value) {
    final Object oldValue = getValue(key);
    setValue(key, value);
    return oldValue;
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
    final List<String> idFieldNames = recordDefinition.getIdFieldNames();
    Identifier.setIdentifier(this, idFieldNames, identifier);
  }

  /**
   * Set the value of the unique identifier field. param id The unique
   * identifier.
   *
   * @param id The unique identifier.
   */

  default void setIdValue(final Object id) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getIdFieldIndex();
    final RecordState state = getState();
    if (state == RecordState.New || state == RecordState.Initalizing) {
      setValue(index, id);
    } else {
      final Object oldId = getValue(index);
      if (oldId != null && !Equals.equal(id, oldId)) {
        throw new IllegalStateException("Cannot change the ID on a persisted record");
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

  default void setValues(final Map<? extends String, ? extends Object> values) {
    if (values != null) {
      for (final String name : new ArrayList<>(values.keySet())) {
        final Object value = values.get(name);
        setValue(name, value);
      }
    }
  }

  default void setValues(final Map<? extends String, ? extends Object> record,
    final Collection<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object oldValue = getValue(fieldName);
      Object newValue = record.get(fieldName);
      if (!EqualsInstance.INSTANCE.equals(oldValue, newValue)) {
        newValue = JavaBeanUtil.clone(newValue);
        setValue(fieldName, newValue);
      }
    }
  }

  default void setValues(final Map<? extends String, ? extends Object> record,
    final String... fieldNames) {
    setValues(record, Arrays.asList(fieldNames));
  }

  default void setValues(final Record record) {
    for (final FieldDefinition fieldDefintion : getRecordDefinition().getFields()) {
      final String name = fieldDefintion.getName();
      Object value = record.getValue(name);
      value = StringConverterRegistry.toObject(fieldDefintion.getType(), value);
      value = JavaBeanUtil.clone(value);
      setValue(name, value);
    }
    setGeometryValue(JavaBeanUtil.clone(record.getGeometry()));
  }

  default void setValuesByPath(final Map<? extends String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<? extends String, ? extends Object> defaultValue : new ArrayList<>(
        values.entrySet())) {
        final String name = defaultValue.getKey();
        final Object value = defaultValue.getValue();
        setValueByPath(name, value);
      }
    }
  }

  @Override
  default int size() {
    return getRecordDefinition().getFieldCount();
  }

  default void validateField(final int fieldIndex) {
    final FieldDefinition field = getFieldDefinition(fieldIndex);
    if (field != null) {
      final Object value = getValue(fieldIndex);
      field.validate(this, value);
    }
  }
}
