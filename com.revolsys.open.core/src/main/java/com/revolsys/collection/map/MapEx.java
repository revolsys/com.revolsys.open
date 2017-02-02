package com.revolsys.collection.map;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.identifier.Identifier;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public interface MapEx extends MapDefault<String, Object> {
  static final MapEx EMPTY = new MapEx() {
    @Override
    public Set<Entry<String, Object>> entrySet() {
      final Map<String, Object> emptyMap = Collections.emptyMap();
      return emptyMap.entrySet();
    }
  };

  default MapEx add(final String key, final Object value) {
    put(key, value);
    return this;
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
    if (Property.hasValue(value)) {
      return Enum.valueOf(enumType, value);
    } else {
      return null;
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
    if (Property.hasValue(value)) {
      return value;
    } else {
      return defaultValue;
    }
  }

  default String getUpperString(final CharSequence fieldName) {
    final String string = getString(fieldName);
    return Strings.upperCase(string);
  }

  /**
   * Get the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @return The field value.
   */
  @SuppressWarnings("unchecked")
  default <T extends Object> T getValue(final CharSequence name) {
    return (T)get(name.toString());
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
}
