package com.revolsys.util.number;

import com.revolsys.util.Property;

public interface Doubles {
  static boolean equal(final double number1, final double number2) {
    if (Double.isNaN(number1)) {
      return Double.isNaN(number2);
    } else if (Double.isInfinite(number1)) {
      return Double.isInfinite(number2);
    } else {
      if (Double.compare(number1, number2) == 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  static boolean equal(final Object number1, final Object number2) {
    return equal((double)number1, (double)number2);
  }

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
