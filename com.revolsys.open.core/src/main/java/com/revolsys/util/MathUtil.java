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
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.LineString;
import com.revolsys.math.Angle;
import com.revolsys.util.number.Doubles;

/**
 * The MathUtil class is a utility class for handling integer, percent and
 * currency BigDecimal values.
 *
 * @author Paul Austin
 */
public interface MathUtil {
  int BYTES_IN_DOUBLE = 8;

  int BYTES_IN_INT = 4;

  int BYTES_IN_LONG = 8;

  int BYTES_IN_SHORT = 2;

  /** The number of cents in a dollar. */
  BigDecimal CURRENCY_CENTS_PER_DOLLAR = getInteger(100);

  /** The scale for currency numbers. */
  int CURRENCY_SCALE = 2;

  /** A 0 currency. */
  BigDecimal CURRENCY0 = getCurrency(0);

  /** The scale for integer numbers. */
  int INTEGER_SCALE = 0;

  /** A 0 integer. */
  BigDecimal INTEGER0 = getInteger(0);

  /** A 1 integer. */
  BigDecimal INTEGER1 = getInteger(1);

  String MAX_DOUBLE_STRING = Doubles.toString(Double.MAX_VALUE);

  String MIN_DOUBLE_STRING = Doubles.toString(-Double.MAX_VALUE);

  /** The scale for percent numbers. */
  int PERCENT_SCALE = 4;

  /** A 0 percent. */
  BigDecimal PERCENT0 = getPercent(0);

  /** A 1000 percent. */
  BigDecimal PERCENT100 = getPercent(1);

  double PI_OVER_2 = Math.PI / 2.0;

  double PI_OVER_4 = Math.PI / 4.0;

  double PI_TIMES_2 = 2.0 * Math.PI;

  Random RANDOM = new Random();

  /**
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  static BigDecimal add(final BigDecimal left, final Number right) {
    return left.add(new BigDecimal(DataTypes.toString(right)));
  }

  @SuppressWarnings("unchecked")
  static <V extends Number> V add(final Number left, final Number right,
    final DataType resultType) {
    final BigDecimal a = getBigDecimal(left);
    final BigDecimal b = getBigDecimal(right);
    final BigDecimal result = a.add(b);
    return (V)resultType.toObject(result);
  }

  static void append(final StringBuilder string, final double number) {
    if (Double.isNaN(number)) {
      string.append("NaN");
    } else {
      DoubleFormatUtil.formatDoublePrecise(number, 19, 19, string);
    }
  }

  static double avg(final double a, final double b) {
    return (a + b) / 2d;
  }

  /**
   * Clamps a <tt>double</tt> value to a given range.
   * @param x the value to clamp
   * @param min the minimum value of the range
   * @param max the maximum value of the range
   * @return the clamped value
   */
  static double clamp(final double x, final double min, final double max) {
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
  static int clamp(final int x, final int min, final int max) {
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
  static String currencyToString(final BigDecimal amount) {
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
  static double distance(final double x1, final double y1, final double x2, final double y2) {
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
  static BigDecimal divideCurrency(final BigDecimal left, final BigDecimal right) {
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
  static BigDecimal dividePercent(final BigDecimal left, final BigDecimal right) {
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
  static BigDecimal dividePercent(final double left, final double right) {
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
  static BigDecimal dividePercent(final double left, final int right) {
    return dividePercent(new BigDecimal(left), new BigDecimal(right));
  }

  static String format(final String pattern, final Number number) {
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
  static double getAngle(final LineString points, final int i1, final int i2, final boolean start) {
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

  static BigDecimal getBigDecimal(final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        final String stringValue = DataTypes.toString(value);
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
  static BigDecimal getCurrency(final BigDecimal amount) {
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
  static BigDecimal getCurrency(final double amount) {
    return getCurrency(new BigDecimal(amount));
  }

  /**
   * Convert a BigDecimal into an ineteger BigDecimal.
   *
   * @param value The BigDecimal value.
   * @return The ineteger BigDecimal.
   */
  static BigDecimal getInteger(final BigDecimal value) {
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
  static BigDecimal getInteger(final int value) {
    return getInteger(new BigDecimal((double)value));
  }

  static Object getMaxValue(final Class<?> dataType) {
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

  static Object getMinValue(final Class<?> dataType) {
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

  static double getNorthClockwiseAngle(final double angle) {
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
  static BigDecimal getPercent(final BigDecimal decimalPercent) {
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
  static BigDecimal getPercent(final double decimalPercent) {
    return getPercent(new BigDecimal(decimalPercent));
  }

  /**
   * Convert a String decimal percent (e.g. 0.5 is 50%) into an percent
   * BigDecimal.
   *
   * @param decimalPercent The decimal percent value.
   * @return The currency.
   */
  static BigDecimal getPercent(final String decimalPercent) {
    return getPercent(new BigDecimal(decimalPercent));
  }

  static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ f >>> 32);
  }

  /** sqrt(a^2 + b^2) without under/overflow. **/

  static double hypot(final double a, final double b) {
    double r;
    if (Math.abs(a) > Math.abs(b)) {
      r = b / a;
      r = Math.abs(a) * Math.sqrt(1 + r * r);
    } else if (b != 0) {
      r = a / b;
      r = Math.abs(b) * Math.sqrt(1 + r * r);
    } else {
      r = 0.0;
    }
    return r;
  }

  /**
   * Convert a BigDecimal integer to a string.
   *
   * @param integer The BigDecimal integer.
   * @return The integer String
   */
  static String integerToString(final BigDecimal integer) {
    return getInteger(integer).toString();
  }

  static boolean isAcute(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final double dx0 = x1 - x2;
    final double dy0 = y1 - y2;
    final double dx1 = x3 - x2;
    final double dy1 = y3 - y2;
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  static boolean isNanOrInfinite(final double... values) {
    for (final double value : values) {
      if (Double.isNaN(value)) {
        return true;
      } else if (Double.isInfinite(value)) {
        return true;
      }
    }
    return false;
  }

  static double max(final double... values) {
    double max = -Double.MAX_VALUE;
    for (final double value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  static int max(final int... values) {
    int max = Integer.MIN_VALUE;
    for (final int value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  static double max(final Iterable<? extends Number> numbers) {
    double max = -Double.MAX_VALUE;
    for (final Number number : numbers) {
      final double value = number.doubleValue();
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  static int maxInt(final Iterable<Integer> numbers) {
    int min = Integer.MIN_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value > min) {
        min = value;
      }
    }
    return min;
  }

  static double midpoint(final double d1, final double d2) {
    return d1 + (d2 - d1) / 2;
  }

  static double min(final double... values) {
    double min = Double.MAX_VALUE;
    for (final double value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  static int min(final int... values) {
    int min = Integer.MAX_VALUE;
    for (final int value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  static double min(final Iterable<? extends Number> numbers) {
    double min = Double.MAX_VALUE;
    for (final Number number : numbers) {
      final double value = number.doubleValue();
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  static int minInt(final Iterable<Integer> numbers) {
    int max = Integer.MAX_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value < max) {
        max = value;
      }
    }
    return max;
  }

  static double orientedAngleBetween(double angle1, double angle2) {
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
  static String percentToString(final BigDecimal decimalPercent) {
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
  static String percentToString(final BigDecimal decimalPercent, final int scale) {
    if (decimalPercent != null) {
      final DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(0);
      format.setMaximumFractionDigits(scale);
      final String string = format.format(
        decimalPercent.multiply(new BigDecimal(100)).setScale(scale, BigDecimal.ROUND_HALF_UP))
        + "%";

      return string;
    } else {
      return null;
    }
  }

  static double pointLineDistance(final double x, final double y, final double x1, final double y1,
    final double x2, final double y2) {
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

  static boolean precisionEqual(final double value1, final double value2, final double scale) {
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

  static double randomGaussian(final double mean, final double variance) {
    return mean + RANDOM.nextGaussian() * variance;
  }

  static double randomRange(final double min, final double max) {
    return min + RANDOM.nextDouble() * (max - min);
  }

  static byte sgn(final byte x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  static int sgn(final double x) {
    if (x > 0.0D) {
      return 1;
    }
    if (x < 0.0D) {
      return -1;
    }
    return 0;
  }

  static int sgn(final float x) {
    if (x > 0.0F) {
      return 1;
    }
    if (x < 0.0F) {
      return -1;
    }
    return 0;
  }

  static int sgn(final int x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  static int sgn(final long x) {
    if (x > 0L) {
      return 1;
    }
    if (x < 0L) {
      return -1;
    }
    return 0;
  }

  static short sgn(final short x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  static <V extends Number> V subtract(final Number left, final Number right,
    final DataType resultType) {
    if (left == null) {
      return null;
    } else if (right == null) {
      return (V)resultType.toObject(left);
    } else {
      final BigDecimal a = getBigDecimal(left);
      final BigDecimal b = getBigDecimal(right);
      final BigDecimal result = a.subtract(b);
      return (V)resultType.toObject(result);
    }
  }

  static Double toDouble(final Object value) {
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

  static double[] toDoubleArray(final List<? extends Number> numbers) {
    final double[] doubles = new double[numbers.size()];
    for (int i = 0; i < doubles.length; i++) {
      final Number number = numbers.get(i);
      doubles[i] = number.doubleValue();
    }
    return doubles;
  }

  static double[] toDoubleArray(final String... values) {
    final double[] doubles = new double[values.length];
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = Double.valueOf(values[i]);
    }
    return doubles;
  }

  static double[] toDoubleArraySplit(final String value) {
    return toDoubleArray(value.split(","));
  }

  static double[] toDoubleArraySplit(final String value, final String regex) {
    return toDoubleArray(value.split(regex));
  }

  static Double toDoubleValue(final Object value) {
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

  static int toInt(final byte[] bytes, final int offset) {
    final byte b1 = bytes[offset];
    final byte b2 = bytes[offset + 1];
    final byte b3 = bytes[offset + 2];
    final byte b4 = bytes[offset + 3];
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | b4 & 0xFF;
  }

  static int[] toIntArray(final List<? extends Number> numbers) {
    final int[] ints = new int[numbers.size()];
    for (int i = 0; i < ints.length; i++) {
      final Number number = numbers.get(i);
      ints[i] = number.intValue();
    }
    return ints;
  }

  static long toLong(final byte[] bytes, final int offset) {
    final long high = (long)toInt(bytes, offset) << 32;
    final long low = (long)toInt(bytes, offset + 4) << 32 >>> 32;
    return high | low;
  }
}
