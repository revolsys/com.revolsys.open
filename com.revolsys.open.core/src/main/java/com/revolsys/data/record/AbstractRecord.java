package com.revolsys.data.record;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.AbstractIdentifier;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.ListIdentifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.query.Value;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecord extends AbstractMap<String, Object> implements Record,
  Cloneable {

  /**
   * Create a clone of the object.
   *
   * @return The cloned object.
   */
  @Override
  public AbstractRecord clone() {
    try {
      final AbstractRecord newObject = (AbstractRecord)super.clone();
      newObject.setState(RecordState.New);
      return newObject;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone", e);
    }
  }

  @Override
  public int compareTo(final Record other) {
    if (this == other) {
      return 0;
    } else {
      final int recordDefinitionCompare = getRecordDefinition().compareTo(
        other.getRecordDefinition());
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
        final Geometry geometry1 = getGeometryValue();
        final Geometry geometry2 = other.getGeometryValue();
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

  @Override
  public void delete() {
    getRecordDefinition().delete(this);
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    final Set<Entry<String, Object>> entries = new LinkedHashSet<Entry<String, Object>>();
    for (int i = 0; i < this.getRecordDefinition().getFieldCount(); i++) {
      entries.add(new RecordMapEntry(this, i));
    }
    return entries;
  }

  @Override
  public boolean equals(final Object o) {
    return this == o;
  }

  @Override
  public Object get(final Object key) {
    if (key instanceof String) {
      final String name = (String)key;
      return getValue(name);
    } else {
      return null;
    }
  }

  @Override
  public Byte getByte(final CharSequence name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.byteValue();
    }
  }

  @Override
  public Double getDouble(final CharSequence name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  /**
   * Get the factory which created the instance.
   *
   * @return The factory.
   */
  @Override
  public RecordFactory getFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordFactory();
    }
  }

  public FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldIndex);
  }

  @Override
  public String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldTitle(name);
  }

  @Override
  public Float getFloat(final CharSequence name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.floatValue();
    }
  }

  /**
   * Get the value of the primary geometry attribute.
   *
   * @return The primary geometry.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Geometry> T getGeometryValue() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final int index = recordDefinition.getGeometryFieldIndex();
      return (T)getValue(index);
    }
  }

  @Override
  public Identifier getIdentifier() {
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
        return SingleIdentifier.create(idValue);
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

  @Override
  public Identifier getIdentifier(final List<String> fieldNames) {
    final int idCount = fieldNames.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final String idFieldName = fieldNames.get(0);
      final Object idValue = getValue(idFieldName);
      if (idValue == null) {
        return null;
      } else {
        return SingleIdentifier.create(idValue);
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

  @Override
  public Identifier getIdentifier(final String... fieldNames) {
    return getIdentifier(Arrays.asList(fieldNames));
  }

  @Override
  public Integer getInteger(final CharSequence name) {
    final Object value = getValue(name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  @Override
  public Long getLong(final CharSequence name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.longValue();
    }
  }

  @Override
  public Short getShort(final CharSequence name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.shortValue();
    }
  }

  @Override
  public String getString(final CharSequence name) {
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

  @Override
  public String getTypePath() {
    return getRecordDefinition().getPath();
  }

  /**
   * Get the value of the attribute with the specified name.
   *
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final CharSequence name) {
    final RecordDefinition recordDefinition = this.getRecordDefinition();
    try {
      final int index = recordDefinition.getFieldIndex(name);
      return (T)getValue(index);
    } catch (final NullPointerException e) {
      LoggerFactory.getLogger(getClass()).warn(
        "Attribute " + recordDefinition.getPath() + "." + name + " does not exist");
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getValueByPath(final CharSequence path) {
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
            final CodeTable codeTable = this.getRecordDefinition().getCodeTableByFieldName(
              propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(SingleIdentifier.create(propertyValue));
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
          final CodeTable codeTable = this.getRecordDefinition().getCodeTableByFieldName(
            propertyName);
          if (codeTable != null) {
            propertyValue = codeTable.getMap(SingleIdentifier.create(propertyValue));
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

  @Override
  public Map<String, Object> getValueMap(final Collection<? extends CharSequence> fieldNames) {
    final Map<String, Object> values = new HashMap<String, Object>();
    for (final CharSequence name : fieldNames) {
      final Object value = getValue(name);
      if (value != null) {
        values.put(name.toString(), value);
      }
    }
    return values;
  }

  @Override
  public List<Object> getValues() {
    final List<Object> values = new ArrayList<Object>();
    for (int i = 0; i < this.getRecordDefinition().getFieldCount(); i++) {
      final Object value = getValue(i);
      values.add(value);
    }
    return values;
  }

  /**
   * Checks to see if the definition for this Record has an attribute with the
   * specified name.
   *
   * @param name The name of the attribute.
   * @return True if the Record has an attribute with the specified name.
   */
  @Override
  public boolean hasField(final CharSequence name) {
    return this.getRecordDefinition().hasField(name);
  }

  @Override
  public boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return Property.hasValue(value);
  }

  @Override
  public boolean hasValuesAll(final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      if (!hasValue(fieldName)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasValuesAny(final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      if (hasValue(fieldName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isModified() {
    if (getState() == RecordState.New) {
      return true;
    } else if (getState() == RecordState.Modified) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isValid(final int index) {
    return true;
  }

  @Override
  public boolean isValid(final String fieldName) {
    return true;
  }

  @Override
  public Object put(final String key, final Object value) {
    final Object oldValue = getValue(key);
    setValue(key, value);
    return oldValue;
  }

  /**
   * Set the value of the primary geometry attribute.
   *
   * @param geometry The primary geometry.
   */
  @Override
  public void setGeometryValue(final Geometry geometry) {
    final int index = this.getRecordDefinition().getGeometryFieldIndex();
    setValue(index, geometry);
  }

  @Override
  public void setIdentifier(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<String> idFieldNames = recordDefinition.getIdFieldNames();
    AbstractIdentifier.setIdentifier(this, idFieldNames, identifier);
  }

  /**
   * Set the value of the unique identifier attribute. param id The unique
   * identifier.
   *
   * @param id The unique identifier.
   */
  @Override
  public void setIdValue(final Object id) {
    final int index = this.getRecordDefinition().getIdFieldIndex();
    if (this.getState() == RecordState.New || this.getState() == RecordState.Initalizing) {
      setValue(index, id);
    } else {
      final Object oldId = getValue(index);
      if (oldId != null && !EqualsRegistry.equal(id, oldId)) {
        throw new IllegalStateException("Cannot change the ID on a persisted object");
      }
    }
  }

  /**
   * Set the value of the attribute with the specified name.
   *
   * @param name The name of the attribute.
   * @param value The new value.
   */
  @Override
  public boolean setValue(final CharSequence name, final Object value) {
    boolean updated = false;
    final int index = this.getRecordDefinition().getFieldIndex(name);
    if (index >= 0) {
      return setValue(index, value);
    } else {

      final int dotIndex = name.toString().indexOf('.');
      if (dotIndex == -1) {

      } else {
        final CharSequence key = name.subSequence(0, dotIndex);
        final CharSequence subKey = name.subSequence(dotIndex + 1, name.length());
        final Object objectValue = getValue(key);
        if (objectValue == null) {
          final DataType fieldType = this.getRecordDefinition().getFieldType(key);
          if (fieldType != null) {
            if (fieldType.getJavaClass() == Record.class) {
              final String typePath = fieldType.getName();
              final RecordDefinitionFactory recordDefinitionFactory = this.getRecordDefinition()
                .getRecordDefinitionFactory();
              final RecordDefinition subRecordDefinition = recordDefinitionFactory.getRecordDefinition(typePath);
              final RecordFactory recordFactory = subRecordDefinition.getRecordFactory();
              final Record subRecord = recordFactory.createRecord(subRecordDefinition);
              updated |= subRecord.setValue(subKey, value);
              updated |= setValue(key, subRecord);
            }
          }
        } else {
          if (objectValue instanceof Geometry) {
            final Geometry geometry = (Geometry)objectValue;
            GeometryProperties.setGeometryProperty(geometry, subKey, value);
            updated = true;
          } else if (objectValue instanceof Record) {
            final Record object = (Record)objectValue;
            updated |= object.setValue(subKey, value);
          } else {
            JavaBeanUtil.setProperty(objectValue, subKey.toString(), value);
            updated = true;
          }
        }
      }
    }
    return updated;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean setValueByPath(final CharSequence path, final Object value) {
    boolean updated = false;
    final String name = path.toString();
    final int dotIndex = name.indexOf(".");
    String codeTableFieldName;
    String codeTableValueName = null;
    if (dotIndex == -1) {
      if (name.equals(getRecordDefinition().getIdFieldName())) {
        codeTableFieldName = null;
      } else {
        codeTableFieldName = name;
      }
    } else {
      codeTableFieldName = name.substring(0, dotIndex);
      codeTableValueName = name.substring(dotIndex + 1);
    }
    final CodeTable codeTable = this.getRecordDefinition().getCodeTableByFieldName(
      codeTableFieldName);
    if (codeTable == null) {
      if (dotIndex != -1) {
        LoggerFactory.getLogger(getClass()).debug(
          "Cannot get code table for " + this.getRecordDefinition().getPath() + "." + name);
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
          id = codeTable.getId(list.toArray());
        } else {
          id = codeTable.getId(value);
        }
        if (id == null) {
          targetValue = value;
        } else {
          targetValue = Value.getValue(id);
        }
      } else {
        targetValue = codeTable.getId(Collections.singletonMap(codeTableValueName, value));
      }
      if (targetValue == null) {
        targetValue = value;
      }
      updated = setValue(codeTableFieldName, targetValue);
    }
    return updated;
  }

  @Override
  public <T> T setValueByPath(final CharSequence attributePath, final Record source,
    final String sourceAttributePath) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValueByPath(sourceAttributePath);
    setValueByPath(attributePath, value);
    return value;
  }

  @Override
  public void setValues(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, Object> entry : new LinkedHashMap<String, Object>(values).entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setValue(name, value);
      }
    }
  }

  @Override
  public void setValues(final Map<String, Object> record, final Collection<String> attributesNames) {
    for (final String fieldName : attributesNames) {
      final Object oldValue = getValue(fieldName);
      Object newValue = record.get(fieldName);
      if (!EqualsInstance.INSTANCE.equals(oldValue, newValue)) {
        newValue = JavaBeanUtil.clone(newValue);
        setValue(fieldName, newValue);
      }
    }
  }

  @Override
  public void setValues(final Map<String, Object> record, final String... fieldNames) {
    setValues(record, Arrays.asList(fieldNames));
  }

  @Override
  public void setValues(final Record object) {
    for (final String name : this.getRecordDefinition().getFieldNames()) {
      final Object value = JavaBeanUtil.clone(object.getValue(name));
      setValue(name, value);
    }
    setGeometryValue(JavaBeanUtil.clone(object.getGeometryValue()));
  }

  @Override
  public void setValuesByPath(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, Object> defaultValue : new LinkedHashMap<String, Object>(values).entrySet()) {
        final String name = defaultValue.getKey();
        final Object value = defaultValue.getValue();
        setValueByPath(name, value);
      }
    }
  }

  /**
   * Return a String representation of the Object. There is no guarantee as to
   * the format of this string.
   *
   * @return The string value.
   */
  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    s.append(this.getRecordDefinition().getPath()).append("(\n");
    for (int i = 0; i < this.getRecordDefinition().getFieldCount(); i++) {
      final Object value = getValue(i);
      if (value != null) {
        s.append(this.getRecordDefinition().getFieldName(i)).append('=').append(value).append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateState() {
    switch (this.getState()) {
      case Persisted:
        this.setState(RecordState.Modified);
      break;
      case Deleted:
        throw new IllegalStateException("Cannot modify an object which has been deleted");
    }
  }

  @Override
  public void validateField(final int fieldIndex) {
    final FieldDefinition field = getFieldDefinition(fieldIndex);
    if (field != null) {
      final Object value = getValue(fieldIndex);
      field.validate(this, value);
    }
  }

}
