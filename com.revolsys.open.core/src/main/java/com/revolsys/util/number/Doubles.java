package com.revolsys.util.number;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.util.DoubleFormatUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class Doubles extends AbstractDataType {
  public static double add(final double left, final Number right) {
    return left + right.doubleValue();
  }

  public static double divide(final double left, final Number right) {
    return left / right.doubleValue();
  }

  public static boolean equal(final double number1, final double number2) {
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

  public static boolean equal(final Object number1, final Object number2) {
    return equal((double)number1, (double)number2);
  }

  public static double mod(final double left, final Number right) {
    return left % right.doubleValue();
  }

  public static double multiply(final double left, final Number right) {
    return left * right.doubleValue();
  }

  public static double subtract(final double left, final Number right) {
    return left - right.doubleValue();
  }

  public static String toString(final double number) {
    final StringBuilder string = new StringBuilder();
    MathUtil.append(string, number);
    return string.toString();
  }

  public static String toString(final double number, final int precision) {
    final StringBuilder string = new StringBuilder();
    DoubleFormatUtil.formatDoublePrecise(number, precision, precision, string);
    return string.toString();
  }

  /**
   * Convert the value to a Double. If the value cannot be converted to a number
   * an exception is thrown
   */
  public static Double toValid(final Object value) {
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
  public static Double toValid(final String string) {
    if (Property.hasValue(string)) {
      return Double.valueOf(string);
    } else {
      return null;
    }
  }

  public Doubles() {
    super("double", Double.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equal((double)value1, (double)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return Double.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return toString((double)value);
  }
}
