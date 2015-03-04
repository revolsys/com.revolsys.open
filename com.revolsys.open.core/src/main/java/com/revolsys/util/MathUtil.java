/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.math.Angle;

/**
 * The MathUtil class is a utility class for handling integer, percent and
 * currency BigDecimal values.
 *
 * @author Paul Austin
 */
public final class MathUtil {

  /**
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal add(final BigDecimal left, final Number right) {
    return left.add(new BigDecimal(StringConverterRegistry.toString(right)));
  }

  @SuppressWarnings("unchecked")
  public static <V extends Number> V add(final Number left, final Number right,
    final Class<V> resultClass) {
    final BigDecimal a = getBigDecimal(left);
    final BigDecimal b = getBigDecimal(right);
    final BigDecimal result = a.add(b);
    return (V)StringConverterRegistry.toObject(resultClass, result);
  }

  public static void append(final StringBuilder string, final double number) {
    if (Double.isNaN(number)) {
      string.append("NaN");
    } else {
      DoubleFormatUtil.formatDoublePrecise(number, 15, 15, string);
    }
  }

  public static double avg(final double a, final double b) {
    return (a + b) / 2d;
  }

  /**
   * Clamps a <tt>double</tt> value to a given range.
   * @param x the value to clamp
   * @param min the minimum value of the range
   * @param max the maximum value of the range
   * @return the clamped value
   */
  public static double clamp(final double x, final double min, final double max) {
    if (x < min) {
      return min;
    }
    if (x > max) {
      return max;
    }
    return x;
  }

  /**
   * Clamps an <tt>int</tt> value to a given range.
   * @param x the value to clamp
   * @param min the minimum value of the range
   * @param max the maximum value of the range
   * @return the clamped value
   */
  public static int clamp(final int x, final int min, final int max) {
    if (x < min) {
      return min;
    }
    if (x > max) {
      return max;
    }
    return x;
  }

  /**
   * Convert a BigDecimal amount to a currency string prefixed by the "$" sign.
   *
   * @param amount The BigDecimal amount.
   * @return The currency String
   */
  public static String currencyToString(final BigDecimal amount) {
    if (amount != null) {
      return "$" + getCurrency(amount);
    } else {
      return null;
    }
  }

  /**
   * Calculate the distance between two coordinates.
   *
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @return The distance.
   */
  public static double distance(final double x1, final double y1, final double x2, final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;

    final double distance = Math.sqrt(dx * dx + dy * dy);
    return distance;
  }

  /**
   * Divide two currency amounts, setting the scale to {@link #CURRENCY_SCALE}
   * and rounding 1/2 u
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal divideCurrency(final BigDecimal left, final BigDecimal right) {
    return left.divide(right, CURRENCY_SCALE, BigDecimal.ROUND_HALF_UP);
  }

  /**
   * Divide two percent amounts, setting the scale to {@link #CURRENCY_SCALE}
   * and rounding 1/2 u
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal dividePercent(final BigDecimal left, final BigDecimal right) {
    return left.divide(right, PERCENT_SCALE, BigDecimal.ROUND_HALF_UP);
  }

  /**
   * Divide two percent amounts, setting the scale to {@link #CURRENCY_SCALE}
   * and rounding 1/2 u
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal dividePercent(final double left, final double right) {
    return dividePercent(new BigDecimal(left), new BigDecimal(right));
  }

  /**
   * Divide two percent amounts, setting the scale to {@link #CURRENCY_SCALE}
   * and rounding 1/2 u
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal dividePercent(final double left, final int right) {
    return dividePercent(new BigDecimal(left), new BigDecimal(right));
  }

  public static String format(final String pattern, final Number number) {
    return new DecimalFormat(pattern).format(number);
  }

  /**
   * Code taken from DRA FME scripts to calculate angles.
   *
   * @param points
   * @param i1
   * @param i2
   * @return
   */
  public static double getAngle(final LineString points, final int i1, final int i2,
    final boolean start) {
    final double x1 = points.getX(i1);
    final double y1 = points.getY(i1);
    final double x2 = points.getX(i2);
    final double y2 = points.getY(i2);
    if (distance(x1, y1, x2, y2) == 0) { // TODO
      if (start) {
        if (i2 + 1 < points.getVertexCount()) {
          return getAngle(points, i1, i2 + 1, start);
        }
      } else {
        if (i1 - 1 > 0) {
          return getAngle(points, i1 - 1, i2, start);
        }
      }
    }
    return Angle.angleNorthDegrees(x1, y1, x2, y2);
  }

  public static BigDecimal getBigDecimal(final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        final String stringValue = StringConverterRegistry.toString(value);
        return new BigDecimal(stringValue);
      } catch (final NumberFormatException e) {
        return null;
      }
    }
  }

  /**
   * Convert a BigDecimal amount into a currency BigDecimal.
   *
   * @param amount The ammount.
   * @return The currency.
   */
  public static BigDecimal getCurrency(final BigDecimal amount) {
    if (amount != null) {
      return amount.setScale(CURRENCY_SCALE, BigDecimal.ROUND_HALF_UP);
    } else {
      return null;
    }
  }

  /**
   * Convert a double amount into a currency BigDecimal.
   *
   * @param amount The ammount.
   * @return The currency.
   */
  public static BigDecimal getCurrency(final double amount) {
    return getCurrency(new BigDecimal(amount));
  }

  /**
   * Convert a BigDecimal into an ineteger BigDecimal.
   *
   * @param value The BigDecimal value.
   * @return The ineteger BigDecimal.
   */
  public static BigDecimal getInteger(final BigDecimal value) {
    if (value != null) {
      return value.setScale(INTEGER_SCALE, BigDecimal.ROUND_DOWN);
    } else {
      return null;
    }
  }

  /**
   * Convert a int into an ineteger BigDecimal.
   *
   * @param value The int value.
   * @return The ineteger BigDecimal.
   */
  public static BigDecimal getInteger(final int value) {
    return getInteger(new BigDecimal((double)value));
  }

  public static Object getMaxValue(final Class<?> dataType) {
    if (dataType == Byte.class || dataType == Byte.TYPE) {
      return Byte.MAX_VALUE;
    } else if (dataType.equals(Short.class) || dataType.equals(Short.TYPE)) {
      return Short.MAX_VALUE;
    } else if (dataType.equals(Integer.class) || dataType.equals(Integer.TYPE)) {
      return Integer.MAX_VALUE;
    } else if (dataType.equals(Long.class) || dataType.equals(Long.TYPE)) {
      return Long.MAX_VALUE;
    } else {
      return null;
    }
  }

  public static Object getMinValue(final Class<?> dataType) {
    if (dataType == Byte.class || dataType == Byte.TYPE) {
      return Byte.MIN_VALUE;
    } else if (dataType.equals(Short.class) || dataType.equals(Short.TYPE)) {
      return Short.MIN_VALUE;
    } else if (dataType.equals(Integer.class) || dataType.equals(Integer.TYPE)) {
      return Integer.MIN_VALUE;
    } else if (dataType.equals(Long.class) || dataType.equals(Long.TYPE)) {
      return Long.MIN_VALUE;
    } else {
      return null;
    }
  }

  public static double getNorthClockwiseAngle(final double angle) {
    final double northAngle = (450 - angle) % 360;
    return northAngle;
  }

  /**
   * Convert a BigDecimal decimal percent (e.g. 0.5 is 50%) into an percent
   * BigDecimal.
   *
   * @param decimalPercent The decimal percent value.
   * @return The currency.
   */
  public static BigDecimal getPercent(final BigDecimal decimalPercent) {
    if (decimalPercent != null) {
      return decimalPercent.setScale(PERCENT_SCALE, BigDecimal.ROUND_HALF_UP);
    } else {
      return null;
    }
  }

  /**
   * Convert a double decimal percent (e.g. 0.5 is 50%) into an percent
   * BigDecimal.
   *
   * @param decimalPercent The decimal percent value.
   * @return The currency.
   */
  public static BigDecimal getPercent(final double decimalPercent) {
    return getPercent(new BigDecimal(decimalPercent));
  }

  /**
   * Convert a String decimal percent (e.g. 0.5 is 50%) into an percent
   * BigDecimal.
   *
   * @param decimalPercent The decimal percent value.
   * @return The currency.
   */
  public static BigDecimal getPercent(final String decimalPercent) {
    return getPercent(new BigDecimal(decimalPercent));
  }

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ f >>> 32);
  }

  /**
   * Convert a BigDecimal integer to a string.
   *
   * @param integer The BigDecimal integer.
   * @return The integer String
   */
  public static String integerToString(final BigDecimal integer) {
    return getInteger(integer).toString();
  }

  public static boolean isAcute(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final double dx0 = x1 - x2;
    final double dy0 = y1 - y2;
    final double dx1 = x3 - x2;
    final double dy1 = y3 - y2;
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  public static boolean isNanOrInfinite(final double... values) {
    for (final double value : values) {
      if (Double.isNaN(value)) {
        return true;
      } else if (Double.isInfinite(value)) {
        return true;
      }
    }
    return false;
  }

  public static double makePrecise(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isInfinite(value)) {
      return value;
    } else if (Double.isNaN(value)) {
      return value;
    } else {
      // final BigDecimal scaleDecimal = new BigDecimal(scale);
      // final double preciseValue = new
      // BigDecimal(value).multiply(scaleDecimal)
      // .setScale(0, RoundingMode.HALF_UP)
      // .divide(scaleDecimal)
      // .doubleValue();

      final double multiple = value * scale;
      // if (multiple < 0) {
      // multiple -= 0.00001;
      // } else {
      // multiple += 0.00001;
      // }
      final long scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    }
  }

  public static double max(final double... values) {
    double max = -Double.MAX_VALUE;
    for (final double value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  public static int max(final int... values) {
    int max = Integer.MIN_VALUE;
    for (final int value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  public static double max(final Iterable<? extends Number> numbers) {
    double max = -Double.MAX_VALUE;
    for (final Number number : numbers) {
      final double value = number.doubleValue();
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  public static int maxInt(final Iterable<Integer> numbers) {
    int min = Integer.MIN_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value > min) {
        min = value;
      }
    }
    return min;
  }

  public static double midpoint(final double d1, final double d2) {
    return d1 + (d2 - d1) / 2;
  }

  public static double min(final double... values) {
    double min = Double.MAX_VALUE;
    for (final double value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  public static int min(final int... values) {
    int min = Integer.MAX_VALUE;
    for (final int value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  public static double min(final Iterable<? extends Number> numbers) {
    double min = Double.MAX_VALUE;
    for (final Number number : numbers) {
      final double value = number.doubleValue();
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  public static int minInt(final Iterable<Integer> numbers) {
    int max = Integer.MAX_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value < max) {
        max = value;
      }
    }
    return max;
  }

  public static double orientedAngleBetween(double angle1, double angle2) {
    if (angle1 < 0) {
      angle1 = PI_TIMES_2 + angle1;
    }
    if (angle2 < 0) {
      angle2 = PI_TIMES_2 + angle2;
    }
    if (angle2 < angle1) {
      angle2 = angle2 + PI_TIMES_2;
    }
    final double angleBetween = angle2 - angle1;
    return angleBetween;
  }

  /**
   * Convert a BigDecimal decimal percent to a percent string suffixed by the
   * "%" sign.
   *
   * @param decimalPercent The BigDecimal percent.
   * @return The percent String
   */
  public static String percentToString(final BigDecimal decimalPercent) {
    return percentToString(decimalPercent, PERCENT_SCALE);
  }

  /**
   * Convert a BigDecimal decimal percent to a percent string suffixed by the
   * "%" sign with the specified number of decimal places.
   *
   * @param decimalPercent The BigDecimal percent.
   * @param scale The number of decimal places to show.
   * @return The percent String
   */
  public static String percentToString(final BigDecimal decimalPercent, final int scale) {
    if (decimalPercent != null) {
      final DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(0);
      format.setMaximumFractionDigits(scale);
      final String string = format.format(decimalPercent.multiply(new BigDecimal(100)).setScale(
        scale, BigDecimal.ROUND_HALF_UP))
        + "%";

      return string;
    } else {
      return null;
    }
  }

  public static double pointLineDistance(final double x, final double y, final double x1,
    final double y1, final double x2, final double y2) {
    // if start==end, then use pt distance
    if (x1 == x2 && y1 == y2) {
      return distance(x, y, x1, y1);
    }

    // otherwise use comgraphics.algorithms Frequently Asked Questions method
    /*
     * (1) AC dot AB r = --------- ||AB||^2 r has the following meaning: r=0 P =
     * A r=1 P = B r<0 P is on the backward extension of AB r>1 P is on the
     * forward extension of AB 0<r<1 P is interior to AB
     */

    final double r = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1))
      / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

    if (r <= 0.0) {
      return distance(x, y, x1, y1);
    }
    if (r >= 1.0) {
      return distance(x, y, x2, y2);
    }

    /*
     * (2) (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) s = ----------------------------- L^2
     * Then the distance from C to P = |s|*L.
     */

    final double s = ((y1 - y) * (x2 - x1) - (x1 - x) * (y2 - y1))
      / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

    return Math.abs(s) * Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
  }

  public static boolean precisionEqual(final double value1, final double value2, final double scale) {
    if (Double.isNaN(value1) && Double.isNaN(value2)) {
      return true;
    } else if (Double.isInfinite(value1) || Double.isInfinite(value2)) {
      return true;
    } else {
      final double multiply1 = value1 * scale;
      final long rounded1 = Math.round(multiply1);

      final double multiply2 = value2 * scale;
      final long rounded2 = Math.round(multiply2);
      if (rounded1 == rounded2) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static double randomGaussian(final double mean, final double variance) {
    return mean + RANDOM.nextGaussian() * variance;
  }

  public static double randomRange(final double min, final double max) {
    return min + RANDOM.nextDouble() * (max - min);
  }

  public static byte sgn(final byte x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  public static int sgn(final double x) {
    if (x > 0.0D) {
      return 1;
    }
    if (x < 0.0D) {
      return -1;
    }
    return 0;
  }

  public static int sgn(final float x) {
    if (x > 0.0F) {
      return 1;
    }
    if (x < 0.0F) {
      return -1;
    }
    return 0;
  }

  public static int sgn(final int x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  public static int sgn(final long x) {
    if (x > 0L) {
      return 1;
    }
    if (x < 0L) {
      return -1;
    }
    return 0;
  }

  public static short sgn(final short x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public static <V extends Number> V subtract(final Number left, final Number right,
    final Class<V> resultClass) {
    if (left == null) {
      return null;
    } else if (right == null) {
      return (V)StringConverterRegistry.toObject(resultClass, left);
    } else {
      final BigDecimal a = getBigDecimal(left);
      final BigDecimal b = getBigDecimal(right);
      final BigDecimal result = a.subtract(b);
      return (V)StringConverterRegistry.toObject(resultClass, result);
    }
  }

  public static Double toDouble(final Object value) {
    if (!Property.hasValue(value)) {
      throw new NumberFormatException("Numbers cannot be empty");
    } else {
      final String string = value.toString();
      if ("NaN".equalsIgnoreCase(string)) {
        return Double.NaN;
      } else if ("-Infinity".equalsIgnoreCase(string)) {
        return Double.NEGATIVE_INFINITY;
      } else if ("Infinity".equalsIgnoreCase(string)) {
        return Double.POSITIVE_INFINITY;
      } else {
        return Double.valueOf(string);
      }
    }
  }

  public static double[] toDoubleArray(final List<? extends Number> numbers) {
    final double[] doubles = new double[numbers.size()];
    for (int i = 0; i < doubles.length; i++) {
      final Number number = numbers.get(i);
      doubles[i] = number.doubleValue();
    }
    return doubles;
  }

  public static double[] toDoubleArray(final String... values) {
    final double[] doubles = new double[values.length];
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = Double.valueOf(values[i]);
    }
    return doubles;
  }

  public static double[] toDoubleArraySplit(final String value) {
    return toDoubleArray(value.split(","));
  }

  public static double[] toDoubleArraySplit(final String value, final String regex) {
    return toDoubleArray(value.split(regex));
  }

  public static Double toDoubleValue(final Object value) {
    if (!Property.hasValue(value)) {
      return null;
    } else {
      final String string = value.toString();
      if ("NaN".equalsIgnoreCase(string)) {
        return Double.NaN;
      } else if ("-Infinity".equalsIgnoreCase(string)) {
        return Double.NEGATIVE_INFINITY;
      } else if ("Infinity".equalsIgnoreCase(string)) {
        return Double.POSITIVE_INFINITY;
      } else {
        return Double.valueOf(string);
      }
    }
  }

  public final static int toInt(final byte[] bytes, final int offset) {
    final byte b1 = bytes[offset];
    final byte b2 = bytes[offset + 1];
    final byte b3 = bytes[offset + 2];
    final byte b4 = bytes[offset + 3];
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | b4 & 0xFF;
  }

  public static final long toLong(final byte[] bytes, final int offset) {
    final long high = (long)toInt(bytes, offset) << 32;
    final long low = (long)toInt(bytes, offset + 4) << 32 >>> 32;
    return high | low;
  }

  public static String toString(final BigDecimal number) {
    return number.toPlainString();
  }

  public static String toString(final BigInteger number) {
    return number.toString();
  }

  public static String toString(final byte number) {
    return String.valueOf(number);
  }

  public static String toString(final double number) {
    final StringBuilder string = new StringBuilder();
    append(string, number);
    return string.toString();
  }

  public static String toString(final double number, final int precision) {
    final StringBuilder string = new StringBuilder();
    DoubleFormatUtil.formatDoublePrecise(number, precision, precision, string);
    return string.toString();
  }

  public static String toString(final float number) {
    final StringBuilder string = new StringBuilder();
    DoubleFormatUtil.formatDoublePrecise(number, 1, 15, string);
    return string.toString();
  }

  public static String toString(final int number) {
    return String.valueOf(number);
  }

  public static String toString(final long number) {
    return String.valueOf(number);
  }

  public static String toString(final Number number) {
    if (number instanceof Byte) {
      final byte b = (Byte)number;
      return toString(b);
    } else if (number instanceof Short) {
      final short s = (Short)number;
      return toString(s);
    } else if (number instanceof Integer) {
      final int i = (Integer)number;
      return toString(i);
    } else if (number instanceof Long) {
      final long l = (Long)number;
      return toString(l);
    } else if (number instanceof Float) {
      final float f = (Float)number;
      return toString(f);
    } else if (number instanceof Double) {
      final double d = (Double)number;
      return toString(d);
    } else if (number instanceof BigInteger) {
      final BigInteger i = (BigInteger)number;
      return toString(i);
    } else if (number instanceof BigDecimal) {
      final BigDecimal i = (BigDecimal)number;
      return toString(i);
    } else {
      final double d = number.doubleValue();
      return toString(d);
    }
  }

  public static String toString(final short number) {
    return String.valueOf(number);
  }

  public static final int BYTES_IN_DOUBLE = 8;

  public static final int BYTES_IN_INT = 4;

  public static final int BYTES_IN_LONG = 8;

  public static final int BYTES_IN_SHORT = 2;

  /** The number of cents in a dollar. */
  public static final BigDecimal CURRENCY_CENTS_PER_DOLLAR = getInteger(100);

  private static final Random RANDOM = new Random();

  /** The scale for currency numbers. */
  public static final int CURRENCY_SCALE = 2;

  /** A 0 currency. */
  public static final BigDecimal CURRENCY0 = getCurrency(0);

  /** The scale for integer numbers. */
  public static final int INTEGER_SCALE = 0;

  /** A 0 integer. */
  public static final BigDecimal INTEGER0 = getInteger(0);

  /** A 1 integer. */
  public static final BigDecimal INTEGER1 = getInteger(1);

  /** The scale for percent numbers. */
  public static final int PERCENT_SCALE = 4;

  /** A 0 percent. */
  public static final BigDecimal PERCENT0 = getPercent(0);

  /** A 1000 percent. */
  public static final BigDecimal PERCENT100 = getPercent(1);

  public static final double PI_OVER_2 = Math.PI / 2.0;

  public static final double PI_OVER_4 = Math.PI / 4.0;

  public static final double PI_TIMES_2 = 2.0 * Math.PI;

  /**
   * Construct a new MathUtil.
   */
  private MathUtil() {
  }

}
