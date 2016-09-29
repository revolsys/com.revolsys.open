package com.revolsys.util.number;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.util.DoubleFormatUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class Floats extends AbstractDataType {
  public static float add(final float left, final Number right) {
    return left + right.floatValue();
  }

  public static float divide(final float left, final Number right) {
    return left / right.floatValue();
  }

  public static boolean equal(final float number1, final float number2) {
    if (Float.isNaN(number1)) {
      return Float.isNaN(number2);
    } else if (Float.isInfinite(number1)) {
      return Float.isInfinite(number2);
    } else {
      if (Float.compare(number1, number2) == 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean equal(final Object number1, final Object number2) {
    return equal((float)number1, (float)number2);
  }

  public static float mod(final float left, final Number right) {
    return left % right.floatValue();
  }

  public static float multiply(final float left, final Number right) {
    return left * right.floatValue();
  }

  public static boolean overlaps(final float min1, final float max1, final float min2,
    final float max2) {
    if (min1 > max1) {
      return overlaps(max1, min1, min2, max2);
    } else if (min2 > max2) {
      return overlaps(min1, max1, max2, min2);
    } else {
      if (min1 <= max2 && min2 <= max1) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static float subtract(final float left, final Number right) {
    return left - right.floatValue();
  }

  public static Float toFloat(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static Float toFloat(final String value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static String toString(final float number) {
    final StringBuilder string = new StringBuilder();
    MathUtil.append(string, number);
    return string.toString();
  }

  public static String toString(final float number, final int precision) {
    final StringBuilder string = new StringBuilder();
    DoubleFormatUtil.formatDoublePrecise(number, precision, precision, string);
    return string.toString();
  }

  /**
   * Convert the value to a Float. If the value cannot be converted to a number
   * an exception is thrown
   */
  public static Float toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.floatValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Float. If the value cannot be converted to a number and exception is thrown.
   */
  public static Float toValid(final String string) {
    if (Property.hasValue(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }

  public Floats() {
    super("float", Float.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equal((float)value1, (float)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return Float.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return toString((float)value);
  }
}
