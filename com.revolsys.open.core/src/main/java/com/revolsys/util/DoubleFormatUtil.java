/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package com.revolsys.util;

import java.io.IOException;
import java.io.Writer;

/**
 * This class implements fast, thread-safe format of a double value
 * with a given number of decimal digits.
 * <p>
 * The contract for the format methods is this one:
 * if the source is greater than or equal to 1 (in absolute value),
 * use the decimals parameter to define the number of decimal digits; else,
 * use the precision parameter to define the number of decimal digits.
 * <p>
 * A few examples (consider decimals being 4 and precision being 8):
 * <ul>
 * <li>0.0 should be rendered as "0"
 * <li>0.1 should be rendered as "0.1"
 * <li>1234.1 should be rendered as "1234.1"
 * <li>1234.1234567 should be rendered as "1234.1235" (note the trailing 5! Rounding!)
 * <li>1234.00001 should be rendered as "1234"
 * <li>0.00001 should be rendered as "0.00001" (here you see the effect of the "precision" parameter)
 * <li>0.00000001 should be rendered as "0.00000001"
 * <li>0.000000001 should be rendered as "0"
 * </ul>
 *
 * Originally authored by Julien Aym&eacute;.
 */
public final class DoubleFormatUtil {

  private static final double[] POWERS_OF_TEN_DOUBLE = new double[30];

  /**
   * Most used power of ten (to avoid the cost of Math.pow(10, n)
   */
  private static final long[] POWERS_OF_TEN_LONG = new long[19];

  static {
    POWERS_OF_TEN_LONG[0] = 1L;
    for (int i = 1; i < POWERS_OF_TEN_LONG.length; i++) {
      POWERS_OF_TEN_LONG[i] = POWERS_OF_TEN_LONG[i - 1] * 10L;
    }
    for (int i = 0; i < POWERS_OF_TEN_DOUBLE.length; i++) {
      POWERS_OF_TEN_DOUBLE[i] = Double.parseDouble("1e" + i);
    }
  }

  final static char[] digits = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
    'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  private static final char[] DigitOnes = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
    '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
    '5', '6', '7', '8', '9',
  };

  private static final char[] DigitTens = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3',
    '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5',
    '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7',
    '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9',
    '9', '9', '9', '9', '9',
  };

  /**
   * Helper method to do the custom rounding used within formatDoublePrecise
   *
   * @param target the buffer to write to
   * @param scale the expected rounding scale
   * @param intP the source integer part
   * @param decP the source decimal part, truncated to scale + 1 digit
   */
  private static void format(final StringBuilder target, int scale, long intP, long decP) {
    if (decP != 0L) {
      // decP is the decimal part of source, truncated to scale + 1 digit.
      // Custom rounding: add 5
      decP += 5L;
      decP /= 10L;
      if (decP >= tenPowDouble(scale)) {
        intP++;
        decP -= tenPow(scale);
      }
      if (decP != 0L) {
        // Remove trailing zeroes
        while (decP % 10L == 0L) {
          decP = decP / 10L;
          scale--;
        }
      }
    }
    target.append(intP);
    if (decP != 0L) {
      target.append('.');
      // Use tenPow instead of tenPowDouble for scale below 18,
      // since the casting of decP to double may cause some imprecisions:
      // E.g. for decP = 9999999999999999L and scale = 17,
      // decP < tenPow(16) while (double) decP == tenPowDouble(16)
      while (scale > 0 && (scale > 18 ? decP < tenPowDouble(--scale) : decP < tenPow(--scale))) {
        // Insert leading zeroes
        target.append('0');
      }
      target.append(decP);
    }
  }

  /**
   * Rounds the given source value at the given precision
   * and writes the rounded value into the given target
   *
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
   * @param target the buffer to write to
   */
  public static void formatDouble(final double source, final int decimals, final int precision,
    final StringBuilder target) {
    final int scale = Math.abs(source) >= 1.0 ? decimals : precision;
    if (tooManyDigitsUsed(source, scale) || tooCloseToRound(source, scale)) {
      formatDoublePrecise(source, decimals, precision, target);
    } else {
      formatDoubleFast(source, decimals, precision, target);
    }
  }

  /**
   * Rounds the given source value at the given precision
   * and writes the rounded value into the given target
   * <p>
   * This method internally uses double precision computation and rounding,
   * so the result may not be accurate (see formatDouble method for conditions).
   *
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
   * @param target the buffer to write to
   */
  public static void formatDoubleFast(double source, final int decimals, final int precision,
    final StringBuilder target) {
    if (isRoundedToZero(source, decimals, precision)) {
      // Will always be rounded to 0
      target.append('0');
      return;
    } else if (Double.isNaN(source) || Double.isInfinite(source)) {
      // Cannot be formated
      target.append(Double.toString(source));
      return;
    }

    final boolean isPositive = source >= 0.0;
    source = Math.abs(source);
    int scale = source >= 1.0 ? decimals : precision;

    long intPart = (long)Math.floor(source);
    final double tenScale = tenPowDouble(scale);
    final double fracUnroundedPart = (source - intPart) * tenScale;
    long fracPart = Math.round(fracUnroundedPart);
    if (fracPart >= tenScale) {
      intPart++;
      fracPart = Math.round(fracPart - tenScale);
    }
    if (fracPart != 0L) {
      // Remove trailing zeroes
      while (fracPart % 10L == 0L) {
        fracPart = fracPart / 10L;
        scale--;
      }
    }

    if (intPart != 0L || fracPart != 0L) {
      // non-zero value
      if (!isPositive) {
        // negative value, insert sign
        target.append('-');
      }
      // append integer part
      target.append(intPart);
      if (fracPart != 0L) {
        // append fractional part
        target.append('.');
        // insert leading zeroes
        while (scale > 0 && fracPart < tenPowDouble(--scale)) {
          target.append('0');
        }
        target.append(fracPart);
      }
    } else {
      target.append('0');
    }
  }

  /**
   * Rounds the given source value at the given precision
   * and writes the rounded value into the given target
   * <p>
   * This method internally uses the String representation of the source value,
   * in order to avoid any double precision computation error.
   *
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
   * @param target the buffer to write to
   */
  public static void formatDoublePrecise(double source, final int decimals, final int precision,
    final StringBuilder target) {
    if (isRoundedToZero(source, decimals, precision)) {
      // Will always be rounded to 0
      target.append('0');
      return;
    } else if (!Double.isFinite(source)) {
      // Cannot be formated
      target.append(source);
      return;
    }

    final boolean negative = source < 0.0;
    if (negative) {
      source = -source;
      // Done once and for all
      target.append('-');
    }
    final int scale = source >= 1.0 ? decimals : precision;

    // The only way to format precisely the double is to use the String
    // representation of the double, and then to do mathematical integer
    // operation on it.
    final String s = Double.toString(source);
    if (source >= 1e-3 && source < 1e7) {
      // Plain representation of double: "intPart.decimalPart"
      final int dot = s.indexOf('.');
      String decS = s.substring(dot + 1);
      int decLength = decS.length();
      if (scale >= decLength) {
        if ("0".equals(decS)) {
          // source is a mathematical integer
          target.append(s.substring(0, dot));
        } else {
          target.append(s);
          // Remove trailing zeroes
          for (int l = target.length() - 1; l >= 0 && target.charAt(l) == '0'; l--) {
            target.setLength(l);
          }
        }
        return;
      } else if (scale + 1 < decLength) {
        // ignore unnecessary digits
        decLength = scale + 1;
        decS = decS.substring(0, decLength);
      }
      final long intP = Long.parseLong(s.substring(0, dot));
      final long decP = Long.parseLong(decS);
      format(target, scale, intP, decP);
    } else {
      // Scientific representation of double: "x.xxxxxEyyy"
      final int dot = s.indexOf('.');
      final int exp = s.indexOf('E');
      int exposant = Integer.parseInt(s.substring(exp + 1));
      final String intS = s.substring(0, dot);
      final String decS = s.substring(dot + 1, exp);
      final int decLength = decS.length();
      if (exposant >= 0) {
        final int digits = decLength - exposant;
        if (digits <= 0) {
          // no decimal part,
          // no rounding involved
          target.append(intS);
          target.append(decS);
          for (int i = -digits; i > 0; i--) {
            target.append('0');
          }
        } else if (digits <= scale) {
          // decimal part precision is lower than scale,
          // no rounding involved
          target.append(intS);
          target.append(decS.substring(0, exposant));
          target.append('.');
          target.append(decS.substring(exposant));
        } else {
          // decimalDigits > scale,
          // Rounding involved
          final long intP = Long.parseLong(intS) * tenPow(exposant)
            + Long.parseLong(decS.substring(0, exposant));
          final long decP = Long.parseLong(decS.substring(exposant, exposant + scale + 1));
          format(target, scale, intP, decP);
        }
      } else {
        // Only a decimal part is supplied
        exposant = -exposant;
        final int digits = scale - exposant + 1;
        if (digits < 0) {
          target.append('0');
        } else if (digits == 0) {
          final long decP = Long.parseLong(intS);
          format(target, scale, 0L, decP);
        } else if (decLength < digits) {
          final long decP = Long.parseLong(intS) * tenPow(decLength + 1)
            + Long.parseLong(decS) * 10;
          format(target, exposant + decLength, 0L, decP);
        } else {
          final long subDecP = Long.parseLong(decS.substring(0, digits));
          final long decP = Long.parseLong(intS) * tenPow(digits) + subDecP;
          format(target, scale, 0L, decP);
        }
      }
    }
  }

  /**
   * Returns the exponent of the given value
   *
   * @param value the value to get the exponent from
   * @return the value's exponent
   */
  public static int getExponant(final double value) {
    // See Double.doubleToRawLongBits javadoc or IEEE-754 spec
    // to have this algorithm
    long exp = Double.doubleToRawLongBits(value) & 0x7ff0000000000000L;
    exp = exp >> 52;
    return (int)(exp - 1023L);
  }

  /**
   * Returns true if the given source value will be rounded to zero
   *
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
   * @return true if the source value will be rounded to zero
   */
  private static boolean isRoundedToZero(final double source, final int decimals,
    final int precision) {
    // Use 4.999999999999999 instead of 5 since in some cases, 5.0 / 1eN > 5e-N
    // (e.g. for N = 37, 42, 45, 66, ...)
    return source == 0.0
      || Math.abs(source) < 4.999999999999999 / tenPowDouble(Math.max(decimals, precision) + 1);
  }

  public static int parseInt(final String string, final int fromIndex, final int toIndex) {
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

  public static long parseLong(final String string, final int fromIndex, final int toIndex) {
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

  /**
   * Returns ten to the power of n
   *
   * @param n the nth power of ten to get
   * @return ten to the power of n
   */
  public static long tenPow(final int n) {
    return n < POWERS_OF_TEN_LONG.length ? POWERS_OF_TEN_LONG[n] : (long)Math.pow(10, n);
  }

  private static double tenPowDouble(final int n) {
    return n < POWERS_OF_TEN_DOUBLE.length ? POWERS_OF_TEN_DOUBLE[n] : Math.pow(10, n);
  }

  /**
   * Returns true if the given source is considered to be too close
   * of a rounding value for the given scale.
   *
   * @param source the source to round
   * @param scale the scale to round at
   * @return true if the source will be potentially rounded at the scale
   */
  private static boolean tooCloseToRound(double source, final int scale) {
    source = Math.abs(source);
    final long intPart = (long)Math.floor(source);
    final double fracPart = (source - intPart) * tenPowDouble(scale);
    final double decExp = Math.log10(source);
    final double range = decExp + scale >= 12 ? .1 : .001;
    final double distanceToRound1 = Math.abs(fracPart - Math.floor(fracPart));
    final double distanceToRound2 = Math.abs(fracPart - Math.floor(fracPart) - 0.5);
    return distanceToRound1 <= range || distanceToRound2 <= range;
    // .001 range: Totally arbitrary range,
    // I never had a failure in 10e7 random tests with this value
    // May be JVM dependent or architecture dependent
  }

  /**
   * Returns true if the rounding is considered to use too many digits
   * of the double for a fast rounding
   *
   * @param source the source to round
   * @param scale the scale to round at
   * @return true if the rounding will potentially use too many digits
   */
  private static boolean tooManyDigitsUsed(final double source, final int scale) {
    // if scale >= 308, 10^308 ~= Infinity
    final double decExp = Math.log10(source);
    return scale >= 308 || decExp + scale >= 14.5;
  }

  /**
   * Helper method to do the custom rounding used within formatDoublePrecise
   *
   * @param writer the buffer to write to
   * @param scale the expected rounding scale
   * @param intP the source integer part
   * @param decP the source decimal part, truncated to scale + 1 digit
   */
  public static void write(final Writer writer, int scale, long intP, long decP)
    throws IOException {
    if (decP != 0L) {
      // decP is the decimal part of source, truncated to scale + 1 digit.
      // Custom rounding: add 5
      decP += 5L;
      decP /= 10L;
      if (decP >= tenPowDouble(scale)) {
        intP++;
        decP -= tenPow(scale);
      }
      if (decP != 0L) {
        // Remove trailing zeroes
        while (decP % 10L == 0L) {
          decP = decP / 10L;
          scale--;
        }
      }
    }
    writePositiveLong(writer, intP);
    if (decP != 0L) {
      writer.write('.');
      // Use tenPow instead of tenPowDouble for scale below 18,
      // since the casting of decP to double may cause some imprecisions:
      // E.g. for decP = 9999999999999999L and scale = 17,
      // decP < tenPow(16) while (double) decP == tenPowDouble(16)
      while (scale > 0 && (scale > 18 ? decP < tenPowDouble(--scale) : decP < tenPow(--scale))) {
        // Insert leading zeroes
        writer.write('0');
      }
      writePositiveLong(writer, decP);
    }
  }

  /**
  * Rounds the given source value at the given precision
  * and writes the rounded value into the given writer
  * @param writer the buffer to write to
  * @param source the source value to round
  * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
  * @param precision the precision to round at (use if abs(source) &lt; 1.0)
  */
  public static void writeDouble(final Writer writer, final double source, final int decimals,
    final int precision) throws IOException {
    final int scale = Math.abs(source) >= 1.0 ? decimals : precision;
    if (tooManyDigitsUsed(source, scale) || tooCloseToRound(source, scale)) {
      writeDoublePrecise(writer, source, decimals, precision);
    } else {
      writeDoubleFast(writer, source, decimals, precision);
    }
  }

  /**
   * Rounds the given source value at the given precision
   * and writes the rounded value into the given writer
   * <p>
   * This method internally uses double precision computation and rounding,
   * so the result may not be accurate (see formatDouble method for conditions).
   * @param writer the buffer to write to
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
   */
  public static void writeDoubleFast(final Writer writer, double source, final int decimals,
    final int precision) throws IOException {
    if (isRoundedToZero(source, decimals, precision)) {
      // Will always be rounded to 0
      writer.write('0');
      return;
    } else if (Double.isNaN(source) || Double.isInfinite(source)) {
      // Cannot be formated
      writer.write(Double.toString(source));
      return;
    }

    final boolean isPositive = source >= 0.0;
    source = Math.abs(source);
    int scale = source >= 1.0 ? decimals : precision;

    long intPart = (long)Math.floor(source);
    final double tenScale = tenPowDouble(scale);
    final double fracUnroundedPart = (source - intPart) * tenScale;
    long fracPart = Math.round(fracUnroundedPart);
    if (fracPart >= tenScale) {
      intPart++;
      fracPart = Math.round(fracPart - tenScale);
    }
    if (fracPart != 0L) {
      // Remove trailing zeroes
      while (fracPart % 10L == 0L) {
        fracPart = fracPart / 10L;
        scale--;
      }
    }

    if (intPart != 0L || fracPart != 0L) {
      // non-zero value
      if (!isPositive) {
        // negative value, insert sign
        writer.write('-');
      }
      // append integer part
      writePositiveLong(writer, intPart);
      if (fracPart != 0L) {
        // append fractional part
        writer.write('.');
        // insert leading zeroes
        while (scale > 0 && fracPart < tenPowDouble(--scale)) {
          writer.write('0');
        }
        writePositiveLong(writer, fracPart);
      }
    } else {
      writer.write('0');
    }
  }

  /**
   * Rounds the given source value at the given precision
   * and writes the rounded value into the given writer
   * <p>
   * This method internally uses the String representation of the source value,
   * in order to avoid any double precision computation error.
   * @param writer the buffer to write to
   * @param source the source value to round
   * @param decimals the decimals to round at (use if abs(source) &ge; 1.0)
   * @param precision the precision to round at (use if abs(source) &lt; 1.0)
  */
  public static void writeDoublePrecise(final Writer writer, double source, final int decimals,
    final int precision) throws IOException {
    if (isRoundedToZero(source, decimals, precision)) {
      // Will always be rounded to 0
      writer.write('0');
    } else if (!Double.isFinite(source)) {
      if (Double.isNaN(source)) {
        writer.write("NaN");
      } else {
        if (source < 0) {
          writer.write("-Infinity");
        } else {
          writer.write("Infinity");
        }
      }
    } else {

      final boolean negative = source < 0.0;
      if (negative) {
        source = -source;
        // Done once and for all
        writer.write('-');
      }
      final int scale = source >= 1.0 ? decimals : precision;

      // The only way to format precisely the double is to use the String
      // representation of the double, and then to do mathematical integer
      // operation on it.
      final String doubleString = Double.toString(source);
      final int doubleStringLength = doubleString.length();
      final int dot = doubleString.indexOf('.');
      final int decimalIndex = dot + 1;
      if (source >= 1e-3 && source < 1e7) {
        // Plain representation of double: "intPart.decimalPart"
        final int decLength = doubleStringLength - dot - 1;
        if (scale >= decLength) {
          if (decLength == 1 && doubleString.charAt(decimalIndex) == '0') {
            // source is a mathematical integer
            writer.write(doubleString, 0, dot);
          } else {
            // Remove trailing zeroes
            int endIndex = doubleStringLength - 1;
            while (endIndex > 0 && doubleString.charAt(endIndex) == '0') {
              endIndex--;
            }
            writer.write(doubleString, 0, endIndex + 1);
          }
        } else {
          final long integerPart = parseLong(doubleString, 0, dot);
          int decimalEndIndex = doubleStringLength;
          if (scale + 1 < decLength) {
            decimalEndIndex = decimalIndex + scale + 1;
          }
          final long decimalPart = parseLong(doubleString, decimalIndex, decimalEndIndex);
          write(writer, scale, integerPart, decimalPart);
        }
      } else {
        // Scientific representation of double: "x.xxxxxEyyy"
        final int exponentIndex = doubleString.indexOf('E', decimalIndex);
        int exposant = parseInt(doubleString, exponentIndex + 1, doubleStringLength);
        final int decLength = exponentIndex - decimalIndex;
        if (exposant >= 0) {
          final int digits = decLength - exposant;
          if (digits <= 0) {
            // no decimal part,
            // no rounding involved
            writer.write(doubleString, 0, dot);
            writer.write(doubleString, decimalIndex, decLength);
            for (int i = -digits; i > 0; i--) {
              writer.write('0');
            }
          } else if (digits <= scale) {
            // decimal part precision is lower than scale,
            // no rounding involved
            writer.write(doubleString, 0, dot);
            writer.write(doubleString, decimalIndex, exposant);
            writer.write('.');
            writer.write(doubleString, decimalIndex + exposant, decLength - exposant);
          } else {
            final long integerPart = parseLong(doubleString, 0, dot);
            // decimalDigits > scale,
            // Rounding involved
            final long intP = integerPart * tenPow(exposant)
              + parseLong(doubleString, decimalIndex, decimalIndex + exposant);
            final long decP = parseLong(doubleString, decimalIndex + exposant,
              decimalIndex + exposant + scale + 1);
            write(writer, scale, intP, decP);
          }
        } else {
          // Only a decimal part is supplied
          exposant = -exposant;
          final int digits = scale - exposant + 1;
          if (digits < 0) {
            writer.write('0');
          } else {
            final long integerPart = parseLong(doubleString, 0, dot);
            if (digits == 0) {
              write(writer, scale, 0L, integerPart);
            } else if (decLength < digits) {
              final long decP = integerPart * tenPow(decLength + 1)
                + parseLong(doubleString, decimalIndex, exponentIndex) * 10;
              write(writer, exposant + decLength, 0L, decP);
            } else {
              final long subDecP = parseLong(doubleString, decimalIndex, decimalIndex + digits);
              final long decP = integerPart * tenPow(digits) + subDecP;
              write(writer, scale, 0L, decP);
            }
          }
        }
      }
    }
  }

  private static void writePositiveLong(final Writer writer, final long number) throws IOException {
    long power = 10;
    int size = 1;
    while (size < 19) {
      if (number < power) {
        break;
      } else {
        size++;
        power = 10 * power;
      }
    }
    final char[] buf = new char[size];
    long i = number;
    long q;
    int r;
    int charPos = size;

    // Get 2 digits/iteration using longs until quotient fits into an int
    while (i > Integer.MAX_VALUE) {
      q = i / 100;
      // really: r = i - (q * 100);
      r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
      i = q;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // Get 2 digits/iteration using ints
    int q2;
    int i2 = (int)i;
    while (i2 >= 65536) {
      q2 = i2 / 100;
      // really: r = i2 - (q * 100);
      r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
      i2 = q2;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i2 <= 65536, i2);
    for (;;) {
      q2 = i2 * 52429 >>> 16 + 3;
      r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
      buf[--charPos] = digits[r];
      i2 = q2;
      if (i2 == 0) {
        break;
      }
    }
    writer.write(buf);
  }

  private DoubleFormatUtil() {
  }
}
