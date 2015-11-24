package com.revolsys.util.number;

import java.math.BigDecimal;

import com.revolsys.datatype.DataTypes;

public interface BigDecimals {
  static boolean isNumber(final Object value) {
    if (value == null) {
      return false;
    } else if (value instanceof Number) {
      return true;
    } else {
      try {
        toValid(value);
        return true;
      } catch (final Throwable t) {
        return false;
      }
    }
  }

  static BigDecimal toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof BigDecimal) {
      final BigDecimal number = (BigDecimal)value;
      return number;
    } else {
      final String string = DataTypes.toString(value);
      return new BigDecimal(string);
    }
  }
}
