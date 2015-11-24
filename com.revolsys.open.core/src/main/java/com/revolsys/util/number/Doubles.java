package com.revolsys.util.number;

import com.revolsys.util.Property;

public interface Doubles {
  /**
   * Convert the value to a Double. If the value cannot be converted to a number
   * an exception is thrown
   */
  static Double toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Double. If the value cannot be converted to a number and exception is thrown.
   */
  static Double toValid(final String string) {
    if (Property.hasValue(string)) {
      return Double.valueOf(string);
    } else {
      return null;
    }
  }

  default double add(final double left, final Number right) {
    return left + right.doubleValue();
  }

  default double divide(final double left, final Number right) {
    return left / right.doubleValue();
  }

  default double mod(final double left, final Number right) {
    return left % right.doubleValue();
  }

  default double multiply(final double left, final Number right) {
    return left * right.doubleValue();
  }

  default double subtract(final double left, final Number right) {
    return left - right.doubleValue();
  }
}
