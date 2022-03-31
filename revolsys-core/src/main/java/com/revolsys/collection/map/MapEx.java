package com.revolsys.collection.map;

import java.sql.Clob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypeProxy;
import org.jeometry.common.data.type.DataTypeValueFactory;
import org.jeometry.common.data.type.DataTypedValue;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Property;

public interface MapEx extends MapDefault<String, Object>, Cloneable, DataTypedValue {
  static MapEx asEx(final Map<String, ? extends Object> map) {
    if (map instanceof MapEx) {
      return (MapEx)map;
    } else {
      return JsonObject.hash();
    }
  }

  default MapEx add(final String key, final Object value) {
    put(key, value);
    return this;
  }

  default MapEx addAll(final Map<String, ? extends Object> map) {
    for (final Entry<String, ? extends Object> entry : map.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      add(key, value);
    }
    return this;
  }

  default MapEx addFieldValue(final String key, final Map<String, Object> source) {
    final Object value = source.get(key);
    return addValue(key, value);
  }

  default MapEx addFieldValue(final String key, final Map<String, Object> source,
    final String sourceKey) {
    final Object value = source.get(sourceKey);
    return addValue(key, value);
  }

  default MapEx addValue(final String key, final Object value) {
    put(key, value);
    return this;
  }

  MapEx clone();

  default int compareValue(final CharSequence fieldName, final Object value) {
    if (containsKey(fieldName)) {
      final Object fieldValue = getValue(fieldName);
      return CompareUtil.compare(fieldValue, value);
    } else {
      return -1;
    }
  }

  default int compareValue(final MapEx map, final CharSequence fieldName) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return compareValue(fieldName, value);
    }
    return -1;
  }

  default int compareValue(final MapEx map, final CharSequence fieldName,
    final boolean nullsFirst) {
    final Comparable<Object> value1 = getValue(fieldName);
    Object value2;
    if (map == null) {
      value2 = null;
    } else {
      value2 = map.getValue(fieldName);
    }
    return CompareUtil.compare(value1, value2, nullsFirst);
  }

  default <V> V ensureValue(final String key, final DataTypeProxy dataType,
    final Supplier<V> supplier) {
    final Object value = getValue(key);
    if (value == null) {
      final V newValue = supplier.get();
      addValue(key, newValue);
      return newValue;
    } else {
      final V convertedValue = dataType.toObject(value);
      if (convertedValue != value) {
        addValue(key, convertedValue);
      }
      return convertedValue;
    }
  }

  default <V> V ensureValue(final String key, final DataTypeValueFactory<V> factory) {
    return ensureValue(key, factory, factory);
  }

  default <V> V ensureValue(final String key, final Supplier<V> supplier) {
    V value = getValue(key);
    if (value == null) {
      value = supplier.get();
      addValue(key, value);
    }
    return value;
  }

  @Override
  @SuppressWarnings("unchecked")
  default boolean equals(final Object object2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final Map<Object, Object> map2 = (Map<Object, Object>)object2;
    final Set<Object> keys = new TreeSet<>();
    keys.addAll(keySet());
    keys.addAll(map2.keySet());
    keys.removeAll(excludeFieldNames);

    for (final Object key : keys) {
      final Object value1 = get(key);
      final Object value2 = map2.get(key);
      if (!DataType.equal(value1, value2, excludeFieldNames)) {
        return false;
      }
    }
    return true;
  }

  default boolean equalValue(final CharSequence fieldName, final Object value) {
    final Object fieldValue = getValue(fieldName);
    return DataType.equal(fieldValue, value);
  }

  default Boolean getBoolean(final CharSequence name) {
    return getValue(name, DataTypes.BOOLEAN);
  }

  default boolean getBoolean(final CharSequence name, final boolean defaultValue) {
    final Object value = getValue(name, DataTypes.BOOLEAN);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  default Byte getByte(final CharSequence name) {
    return getValue(name, DataTypes.BYTE);
  }

  default byte getByte(final CharSequence name, final byte defaultValue) {
    final Byte value = getByte(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Double getDouble(final CharSequence name) {
    return getValue(name, DataTypes.DOUBLE);
  }

  default double getDouble(final CharSequence name, final double defaultValue) {
    final Double value = getDouble(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default <E extends Enum<E>> E getEnum(final Class<E> enumType, final CharSequence fieldName) {
    final String value = getString(fieldName);
    if (value == null) {
      return null;
    } else {
      return Enum.valueOf(enumType, value);
    }
  }

  default Float getFloat(final CharSequence name) {
    return getValue(name, DataTypes.FLOAT);
  }

  default float getFloat(final CharSequence name, final float defaultValue) {
    final Float value = getFloat(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Identifier getIdentifier(final CharSequence fieldName) {
    final Object value = getValue(fieldName);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final CharSequence fieldName, final DataType dataType) {
    final Object value = getValue(fieldName, dataType);
    return Identifier.newIdentifier(value);
  }

  default Instant getInstant(final CharSequence name) {
    return getValue(name, DataTypes.INSTANT);
  }

  default Integer getInteger(final CharSequence name) {
    return getValue(name, DataTypes.INT);
  }

  default int getInteger(final CharSequence name, final int defaultValue) {
    final Integer value = getInteger(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default JsonList getJsonList(final CharSequence name) {
    return getValue(name, Json.JSON_LIST);
  }

  default JsonList getJsonList(final CharSequence name, final JsonList defaultValue) {
    final JsonList value = getJsonList(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default JsonObject getJsonObject(final CharSequence name) {
    return getValue(name, Json.JSON_OBJECT);
  }

  default JsonObject getJsonObject(final CharSequence name, final JsonObject defaultValue) {
    final JsonObject value = getJsonObject(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Long getLong(final CharSequence name) {
    return getValue(name, DataTypes.LONG);
  }

  default long getLong(final CharSequence name, final long defaultValue) {
    final Long value = getLong(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Short getShort(final CharSequence name) {
    return getValue(name, DataTypes.SHORT);
  }

  default short getShort(final CharSequence name, final short defaultValue) {
    final Short value = getShort(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
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
      return DataTypes.toString(value);
    }
  }

  default String getString(final CharSequence name, final String defaultValue) {
    final String value = getString(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default String getUpperString(final CharSequence fieldName) {
    final String string = getString(fieldName);
    if (string == null) {
      return null;
    } else {
      return string.toUpperCase();
    }
  }

  default UUID getUUID(final CharSequence name) {
    return getValue(name, DataTypes.UUID);
  }

  /**
   * Get the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @return The field value.
   */
  default <T extends Object> T getValue(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      final String nameString = name.toString();
      return getValue(nameString);
    }
  }

  default <T extends Object> T getValue(final CharSequence name, final DataType dataType) {
    final Object value = getValue(name);
    return dataType.toObject(value);
  }

  default <T extends Object> T getValue(final CharSequence name, final DataType dataType,
    final T defaultValue) {
    final T value = getValue(name, dataType);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default <T extends Object> T getValue(final CharSequence name,
    final DataTypeValueFactory<T> defaultValueFactory) {
    final DataType dataType = defaultValueFactory.getDataType();
    final T value = getValue(name, dataType);
    if (value == null) {
      return defaultValueFactory.get();
    } else {
      return value;
    }
  }

  default <T extends Object> T getValue(final CharSequence name, final Supplier<T> defaultValue) {
    final T value = getValue(name);
    if (value == null) {
      return defaultValue.get();
    } else {
      return value;
    }
  }

  default <T extends Object> T getValue(final CharSequence name, final T defaultValue) {
    final T value = getValue(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  @SuppressWarnings("unchecked")
  default <T extends Object> T getValue(final String name) {
    return (T)get(name);
  }

  @SuppressWarnings("unchecked")
  default <T> T getValueByPath(final CharSequence path) {
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
          }
        } else {
          return null;
        }
      } else if (propertyValue instanceof Map) {
        final Map<String, Object> map = (Map<String, Object>)propertyValue;
        propertyValue = map.get(propertyName);
        if (propertyValue == null) {
          return null;
        }
      } else {
        try {
          final Object object = propertyValue;
          propertyValue = Property.getSimple(object, propertyName);
        } catch (final IllegalArgumentException e) {
          Logs.debug(this, "Path does not exist " + path, e);
          return null;
        }
      }
    }
    return (T)propertyValue;
  }

  default boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return value != null;
  }

  default boolean hasValuesAll(final CharSequence... names) {
    if (names == null || names.length == 0) {
      return false;
    } else {
      for (final CharSequence name : names) {
        if (!hasValue(name)) {
          return false;
        }
      }
      return true;
    }
  }

  default boolean hasValuesAny(final CharSequence... names) {
    if (names == null || names.length == 0) {
      return false;
    } else {
      for (final CharSequence name : names) {
        if (hasValue(name)) {
          return true;
        }
      }
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  default <T extends Object> T removeValue(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      return (T)remove(name.toString());
    }
  }

  default <T extends Object> T removeValue(final CharSequence name, final DataType dataType) {
    final Object value = removeValue(name);
    return dataType.toObject(value);
  }

  default <V> V removeValue(final CharSequence name, final Supplier<V> defaultValue) {
    final V value = removeValue(name);
    if (value == null) {
      return defaultValue.get();
    } else {
      return value;
    }
  }

  default <V> V removeValue(final CharSequence name, final V defaultValue) {
    final V value = removeValue(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }
}
