package com.revolsys.collection.map;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypedValue;
import org.jeometry.common.data.type.DataTypes;

public interface MapEx extends MapDefault<String, Object>, Cloneable, DataTypedValue {
  static final MapEx EMPTY = new MapEx() {
    @Override
    public MapEx clone() {
      return this;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      final Map<String, Object> emptyMap = Collections.emptyMap();
      return emptyMap.entrySet();
    }

    @Override
    public String toString() {
      return "{}";
    }
  };

  static MapEx asEx(final Map<String, ? extends Object> map) {
    if (map instanceof MapEx) {
      return (MapEx)map;
    } else {
      return new LinkedHashMapEx();
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

  MapEx clone();

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

  /**
   * Get the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @return The field value.
   */
  @SuppressWarnings("unchecked")
  default <T extends Object> T getValue(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      return (T)get(name.toString());
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

  default boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return value != null;
  }
}
