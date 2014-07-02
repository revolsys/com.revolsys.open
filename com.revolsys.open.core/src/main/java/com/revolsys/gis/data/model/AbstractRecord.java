package com.revolsys.gis.data.model;

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
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractRecord extends AbstractMap<String, Object>
  implements DataObject, Cloneable {

  /**
   * Create a clone of the object.
   *
   * @return The cloned object.
   */
  @Override
  public BaseRecord clone() {
    try {
      final BaseRecord newObject = (BaseRecord)super.clone();
      newObject.setState(DataObjectState.New);
      return newObject;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone", e);
    }
  }

  @Override
  public int compareTo(final DataObject other) {
    if (this == other) {
      return 0;
    } else {
      final int metaDataCompare = getMetaData().compareTo(other.getMetaData());
      if (metaDataCompare == 0) {
        final RecordIdentifier id1 = getIdentifier();
        final RecordIdentifier id2 = other.getIdentifier();
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
        return metaDataCompare;
      }
    }

  }

  @Override
  public void delete() {
    getMetaData().delete(this);
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    final Set<Entry<String, Object>> entries = new LinkedHashSet<Entry<String, Object>>();
    for (int i = 0; i < this.getMetaData().getAttributeCount(); i++) {
      entries.add(new DataObjectEntry(this, i));
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
  public String getAttributeTitle(final String name) {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getAttributeTitle(name);
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
  public DataObjectFactory getFactory() {
    final DataObjectMetaData metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      return metaData.getDataObjectFactory();
    }
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
    final int index = this.getMetaData().getGeometryAttributeIndex();
    return (T)getValue(index);
  }

  @Override
  public RecordIdentifier getIdentifier() {
    final DataObjectMetaData metaData = getMetaData();
    final List<Integer> idAttributeIndexes = metaData.getIdAttributeIndexes();
    final int idCount = idAttributeIndexes.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final Integer idAttributeIndex = idAttributeIndexes.get(0);
      final Object idValue = getValue(idAttributeIndex);
      if (idValue == null) {
        return null;
      } else {
        return SingleRecordIdentifier.create(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final Integer idAttributeIndex = idAttributeIndexes.get(i);
        final Object value = getValue(idAttributeIndex);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListRecordIdentifier(idValues);
      } else {
        return null;
      }
    }
  }

  @Override
  public RecordIdentifier getIdentifier(final List<String> attributeNames) {
    final int idCount = attributeNames.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final String idAttributeName = attributeNames.get(0);
      final Object idValue = getValue(idAttributeName);
      if (idValue == null) {
        return null;
      } else {
        return SingleRecordIdentifier.create(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final String idAttributeName = attributeNames.get(i);
        final Object value = getValue(idAttributeName);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListRecordIdentifier(idValues);
      } else {
        return null;
      }
    }
  }

  @Override
  public RecordIdentifier getIdentifier(final String... attributeNames) {
    return getIdentifier(Arrays.asList(attributeNames));
  }

  /**
   * Get the value of the unique identifier attribute.
   *
   * @return The unique identifier.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getIdValue() {
    final int index = this.getMetaData().getIdAttributeIndex();
    return (T)getValue(index);
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
  public String getTypeName() {
    return getMetaData().getPath();
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
    try {
      final int index = this.getMetaData().getAttributeIndex(name);
      return (T)getValue(index);
    } catch (final NullPointerException e) {
      LoggerFactory.getLogger(getClass()).warn(
        "Attribute " + this.getMetaData().getPath() + "." + name
        + " does not exist");
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
      if (propertyValue instanceof DataObject) {
        final DataObject dataObject = (DataObject)propertyValue;

        if (dataObject.hasAttribute(propertyName)) {
          propertyValue = dataObject.getValue(propertyName);
          if (propertyValue == null) {
            return null;
          } else if (i + 1 < propertyPath.length) {
            final CodeTable codeTable = this.getMetaData()
                .getCodeTableByColumn(propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(SingleRecordIdentifier.create(propertyValue));
            }
          }
        } else {
          return null;
        }
      } else if (propertyValue instanceof Geometry) {
        final Geometry geometry = (Geometry)propertyValue;
        propertyValue = GeometryProperties.getGeometryProperty(geometry,
          propertyName);
      } else if (propertyValue instanceof Map) {
        final Map<String, Object> map = (Map<String, Object>)propertyValue;
        propertyValue = map.get(propertyName);
        if (propertyValue == null) {
          return null;
        } else if (i + 1 < propertyPath.length) {
          final CodeTable codeTable = this.getMetaData().getCodeTableByColumn(
            propertyName);
          if (codeTable != null) {
            propertyValue = codeTable.getMap(SingleRecordIdentifier.create(propertyValue));
          }
        }
      } else {
        try {
          propertyValue = JavaBeanUtil.getProperty(propertyValue, propertyName);
        } catch (final IllegalArgumentException e) {
          LoggerFactory.getLogger(getClass()).error(
            "Path does not exist " + path, e);
          return null;
        }
      }
    }
    return (T)propertyValue;
  }

  @Override
  public Map<String, Object> getValueMap(
    final Collection<? extends CharSequence> attributeNames) {
    final Map<String, Object> values = new HashMap<String, Object>();
    for (final CharSequence name : attributeNames) {
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
    for (int i = 0; i < this.getMetaData().getAttributeCount(); i++) {
      final Object value = getValue(i);
      values.add(value);
    }
    return values;
  }

  /**
   * Checks to see if the metadata for this DataObject has an attribute with the
   * specified name.
   *
   * @param name The name of the attribute.
   * @return True if the DataObject has an attribute with the specified name.
   */
  @Override
  public boolean hasAttribute(final CharSequence name) {
    return this.getMetaData().hasAttribute(name);
  }

  @Override
  public boolean isModified() {
    if (getState() == DataObjectState.New) {
      return true;
    } else if (getState() == DataObjectState.Modified) {
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
  public boolean isValid(final String attributeName) {
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
    final int index = this.getMetaData().getGeometryAttributeIndex();
    setValue(index, geometry);
  }

  @Override
  public void setIdentifier(final RecordIdentifier identifier) {
    final DataObjectMetaData metaData = getMetaData();
    final List<String> idAttributeNames = metaData.getIdAttributeNames();
    AbstractRecordIdentifier.setIdentifier(this, idAttributeNames, identifier);
  }

  /**
   * Set the value of the unique identifier attribute. param id The unique
   * identifier.
   *
   * @param id The unique identifier.
   */
  @Override
  public void setIdValue(final Object id) {
    final int index = this.getMetaData().getIdAttributeIndex();
    if (this.getState() == DataObjectState.New
        || this.getState() == DataObjectState.Initalizing) {
      setValue(index, id);
    } else {
      final Object oldId = getValue(index);
      if (oldId != null && !EqualsRegistry.equal(id, oldId)) {
        throw new IllegalStateException(
            "Cannot change the ID on a persisted object");
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
  public void setValue(final CharSequence name, final Object value) {
    final int index = this.getMetaData().getAttributeIndex(name);
    if (index >= 0) {
      setValue(index, value);
    } else {

      final int dotIndex = name.toString().indexOf('.');
      if (dotIndex == -1) {

      } else {
        final CharSequence key = name.subSequence(0, dotIndex);
        final CharSequence subKey = name.subSequence(dotIndex + 1,
          name.length());
        final Object objectValue = getValue(key);
        if (objectValue == null) {
          final DataType attributeType = this.getMetaData().getAttributeType(
            key);
          if (attributeType != null) {
            if (attributeType.getJavaClass() == DataObject.class) {
              final String typePath = attributeType.getName();
              final DataObjectMetaDataFactory metaDataFactory = this.getMetaData()
                  .getDataObjectMetaDataFactory();
              final DataObjectMetaData subMetaData = metaDataFactory.getMetaData(typePath);
              final DataObjectFactory dataObjectFactory = subMetaData.getDataObjectFactory();
              final DataObject subObject = dataObjectFactory.createDataObject(subMetaData);
              subObject.setValue(subKey, value);
              setValue(key, subObject);
            }
          }
        } else {
          if (objectValue instanceof Geometry) {
            final Geometry geometry = (Geometry)objectValue;
            GeometryProperties.setGeometryProperty(geometry, subKey, value);
          } else if (objectValue instanceof DataObject) {
            final DataObject object = (DataObject)objectValue;
            object.setValue(subKey, value);
          } else {
            JavaBeanUtil.setProperty(objectValue, subKey.toString(), value);
          }
        }
      }
    }
  }

  @Override
  public <T> T setValueByPath(final CharSequence attributePath,
    final DataObject source, final String sourceAttributePath) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValueByPath(sourceAttributePath);
    setValueByPath(attributePath, value);
    return value;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setValueByPath(final CharSequence path, final Object value) {
    final String name = path.toString();
    final int dotIndex = name.indexOf(".");
    String codeTableAttributeName;
    String codeTableValueName = null;
    if (dotIndex == -1) {
      if (name.equals(getMetaData().getIdAttributeName())) {
        codeTableAttributeName = null;
      } else {
        codeTableAttributeName = name;
      }
    } else {
      codeTableAttributeName = name.substring(0, dotIndex);
      codeTableValueName = name.substring(dotIndex + 1);
    }
    final CodeTable codeTable = this.getMetaData().getCodeTableByColumn(
      codeTableAttributeName);
    if (codeTable == null) {
      if (dotIndex != -1) {
        LoggerFactory.getLogger(getClass()).debug(
          "Cannot get code table for " + this.getMetaData().getPath() + "."
              + name);
        return;
      }
      setValue(name, value);
    } else if (value == null || !StringUtils.hasText(value.toString())) {
      setValue(codeTableAttributeName, null);
    } else {
      Object targetValue;
      if (codeTableValueName == null) {
        RecordIdentifier id;
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
        targetValue = codeTable.getId(Collections.singletonMap(
          codeTableValueName, value));
      }
      if (targetValue == null) {
        targetValue = value;
      }
      setValue(codeTableAttributeName, targetValue);
    }
  }

  @Override
  public void setValues(final DataObject object) {
    for (final String name : this.getMetaData().getAttributeNames()) {
      final Object value = JavaBeanUtil.clone(object.getValue(name));
      setValue(name, value);
    }
    setGeometryValue(JavaBeanUtil.clone(object.getGeometryValue()));
  }

  @Override
  public void setValues(final DataObject object,
    final Collection<String> attributesNames) {
    for (final String attributeName : attributesNames) {
      final Object oldValue = getValue(attributeName);
      Object newValue = object.getValue(attributeName);
      if (!EqualsInstance.INSTANCE.equals(oldValue, newValue)) {
        newValue = JavaBeanUtil.clone(newValue);
        setValue(attributeName, newValue);
      }
    }
  }

  @Override
  public void setValues(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, Object> defaultValue : new LinkedHashMap<String, Object>(
          values).entrySet()) {
        final String name = defaultValue.getKey();
        final Object value = defaultValue.getValue();
        setValue(name, value);
      }
    }
  }

  @Override
  public void setValuesByPath(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, Object> defaultValue : new LinkedHashMap<String, Object>(
          values).entrySet()) {
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
    final StringBuffer s = new StringBuffer();
    s.append(this.getMetaData().getPath()).append("(\n");
    for (int i = 0; i < this.getMetaData().getAttributeCount(); i++) {
      final Object value = getValue(i);
      if (value != null) {
        s.append(this.getMetaData().getAttributeName(i))
        .append('=')
        .append(value)
        .append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateState() {
    switch (this.getState()) {
      case Persisted:
        this.setState(DataObjectState.Modified);
        break;
      case Deleted:
        throw new IllegalStateException(
            "Cannot modify an object which has been deleted");
    }
  }

}
