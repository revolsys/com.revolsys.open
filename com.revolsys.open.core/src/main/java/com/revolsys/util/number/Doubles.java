package com.revolsys.util.number;

import java.io.IOException;
import java.io.Writer;

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
    if (Double.compare(number1, number2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean equal(final Object number1, final Object number2) {
    return equal((double)number1, (double)number2);
  }

  public static double makePrecise(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double makePreciseCeil(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double makePreciseFloor(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double mod(final double left, final Number right) {
    return left % right.doubleValue();
  }

  public static double multiply(final double left, final Number right) {
    return left * right.doubleValue();
  }

  public static boolean overlaps(final double min1, final double max1, final double min2,
    final double max2) {
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

  private static int parseInt(final String string, final int fromIndex, final int toIndex) {
    int number = 0;
    int index = fromIndex;
    boolean negative = false;
    if (string.charAt(index) == '-') {
      negative = true;
      index++;
    }
    while (index < toIndex) {
      final int digit = string.charAt(index++) - '0';
      number = number * 10 + digit;
    }
    if (negative) {
      return -number;
    } else {
      return number;
    }
  }

  private static long parseLong(final String string, final int fromIndex, final int toIndex) {
    long number = 0;
    int index = fromIndex;
    boolean negative = false;
    if (string.charAt(index) == '-') {
      negative = true;
      index++;
    }
    while (index < toIndex) {
      final int digit = string.charAt(index++) - '0';
      number = number * 10 + digit;
    }
    if (negative) {
      return -number;
    } else {
      return number;
    }
  }

  public static double subtract(final double left, final Number right) {
    return left - right.doubleValue();
  }

  public static Double toDouble(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static Double toDouble(final String value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
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

  public static void write(final Writer writer, final double number) throws IOException {
    int numberStartIndex = 0;

    // The only way to format precisely the double is to use the String
    // representation of the double, and then to do mathematical integer
    // operation on it.
    final String doubleString = Double.toString(number);
    if (doubleString.charAt(0) == '-') {
      writer.write('-');
      numberStartIndex++;
    }
    final int doubleStringLength = doubleString.length();
    int exponentIndex = -1;
    if (doubleStringLength > 4) {
      final int eMaxIndex = doubleStringLength - 2;
      int eMinIndex = eMaxIndex - 3;
      if (eMinIndex < 2) {
        eMinIndex = 2;
      }
      for (int i = eMaxIndex; i >= eMinIndex; i--) {
        final char c = doubleString.charAt(i);
        if (c == 'E') {
          exponentIndex = i;
          break;
        }
      }
    }
    if (exponentIndex == -1) {
      // Plain representation of double: "intPart.decimalPart"
      if (doubleString.charAt(doubleStringLength - 1) == '0'
        && doubleString.charAt(doubleStringLength - 2) == '.') {
        // source is a mathematical integer
        writer.write(doubleString, numberStartIndex, doubleStringLength - numberStartIndex - 2);
      } else {
        writer.write(doubleString, numberStartIndex, doubleStringLength - numberStartIndex);
      }
    } else {
      int exposant = parseInt(doubleString, exponentIndex + 1, doubleStringLength);
      final int decLength = exponentIndex - 2 - numberStartIndex;
      final int decimalStartIndex = numberStartIndex + 2;
      if (exposant >= 0) {
        final int digits = decLength - exposant;
        if (digits <= 0) {
          // no decimal part,
          writer.write(doubleString, numberStartIndex, 1);
          writer.write(doubleString, decimalStartIndex, decLength);
          for (int i = -digits; i > 0; i--) {
            writer.write('0');
          }
        } else {
          writer.write(doubleString, numberStartIndex, 1);
          writer.write(doubleString, decimalStartIndex, exposant);
          writer.write('.');
          writer.write(doubleString, decimalStartIndex + exposant, decLength - exposant);
        }
      } else {
        // Only a decimal part is supplied
        exposant = -exposant;
        final int digits = 19 - exposant + 1;
        if (digits < 0) {
          writer.write('0');
        } else {
          final int integerPart = doubleString.charAt(numberStartIndex) - '0';
          if (digits == 0) {
            DoubleFormatUtil.write(writer, 19, 0L, integerPart);
          } else if (decLength < digits) {
            final long decP = integerPart * DoubleFormatUtil.tenPow(decLength + 1)
              + parseLong(doubleString, decimalStartIndex, exponentIndex) * 10;
            DoubleFormatUtil.write(writer, exposant + decLength, 0L, decP);
          } else {
            final long subDecP = parseLong(doubleString, decimalStartIndex,
              decimalStartIndex + digits);
            final long decP = integerPart * DoubleFormatUtil.tenPow(digits) + subDecP;
            DoubleFormatUtil.write(writer, 19, 0L, decP);
          }
        }
      }
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
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String string = DataTypes.toString(value);
      if (Property.hasValue(string)) {
        return Double.valueOf(string);
      } else {
        return null;
      }
    }
  }

  @Override
  protected String toStringDo(final Object value) {
    return toString((double)value);
  }
}
