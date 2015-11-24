package com.revolsys.util;

import com.revolsys.datatype.DataTypes;

public interface Booleans {
  static boolean getBoolean(final Object value) {
    final Boolean bool = valueOf(value);
    return bool != null && bool;
  }

  static boolean isFalse(final Object value) {
    final Boolean bool = valueOf(value);
    if (bool == null) {
      return false;
    } else {
      return !bool;
    }
  }

  static boolean isTrue(final Object value) {
    final Boolean bool = valueOf(value);
    if (bool == null) {
      return false;
    } else {
      return bool;
    }
  }

  static Boolean valueOf(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      final String string = DataTypes.toString(value);
      return valueOf(string);
    }
  }

  static Boolean valueOf(final String string) {
    if (Property.hasValue(string)) {
      if ("on".equals(string)) {
        return true;
      } else {
        return Boolean.valueOf(string);
      }
    } else {
      return null;
    }
  }
}
