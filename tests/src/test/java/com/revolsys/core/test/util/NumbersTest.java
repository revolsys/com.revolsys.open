package com.revolsys.core.test.util;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.util.DoubleFormatUtil;
import com.revolsys.util.number.Doubles;
import com.revolsys.util.number.Integers;

public class NumbersTest {
  private void assertOverlaps(final int min1, final int max1, final int min2, final int max2,
    final boolean expected) {
    final boolean actual = Integers.overlaps(min1, max1, min2, max2);
    final String message = min1 + "-" + max1 + " " + min2 + "-" + max2;
    Assert.assertEquals(message, expected, actual);
  }

  private int assertToString(final double number, final String expected) {
    final String doubleString = Doubles.toString(number);
    Assert.assertEquals("Doubles.toString", expected, doubleString);
    try (
      StringWriter writer = new StringWriter()) {
      Doubles.write(writer, number);
      final String writerString = writer.toString();
      Assert.assertEquals("Doubles.write", expected, writerString);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 1;

  }

  private int assertToString(final double number, final String expected, final int decimalPlaces) {
    final StringBuilder string = new StringBuilder();
    DoubleFormatUtil.formatDoublePrecise(number, decimalPlaces, decimalPlaces, string);
    final String doubleString = string.toString();
    Assert.assertEquals("DoubleFormatUtil.formatDoublePrecise", expected, doubleString);
    try (
      StringWriter writer = new StringWriter()) {
      DoubleFormatUtil.writeDoublePrecise(writer, number, decimalPlaces, decimalPlaces);
      final String writerString = writer.toString();
      Assert.assertEquals("DoubleFormatUtil.writeDoublePrecise", expected, writerString);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 1;
  }

  @Test
  public void testDoubleToString() {
    final long time = System.currentTimeMillis();
    int count = 0;
    count += assertToString(-9999648, "-9999648");
    for (final double d : new double[] {
      0.001, 1e7 - 0.1, 0.10
    }) {
      final String s = Double.toString(d);
      count += assertToString(d, s);
    }

    count += assertToString(0.0, "0");
    count += assertToString(1.654321, "1.654", 3);
    count += assertToString(2.147483648E9, "2147483648");
    count += assertToString(2.147483648E8, "214748364.8");
    count += assertToString(2.147483648E8, "214748365", 0);
    count += assertToString(0.0000000001, "0.0000000001");

    final int increment = 1000;
    final int maxInt = Integer.MAX_VALUE - increment;
    for (int i = Integer.MIN_VALUE; i < maxInt; i += increment) {
      final String s = Integer.toString(i);
      count += assertToString(i, s);
      final long l = i + 4 * (long)Integer.MAX_VALUE;
      final String sl = Long.toString(l);
      count += assertToString(l, sl);

    }

    count += assertToString(Integer.MAX_VALUE, Integer.toString(Integer.MAX_VALUE));
    count += assertToString(Double.NaN, "NaN");
    count += assertToString(Double.NEGATIVE_INFINITY, "-Infinity");
    count += assertToString(Double.POSITIVE_INFINITY, "Infinity");

    final long ellapsedTime = System.currentTimeMillis() - time;
    System.out.println(Doubles.toString((double)ellapsedTime / count));
  }

  @Test
  public void testOverlaps() {
    // same range and min/max reversed
    assertOverlaps(0, 10, 0, 10, true);
    assertOverlaps(10, 0, 0, 10, true);
    assertOverlaps(0, 10, 10, 0, true);
    assertOverlaps(10, 0, 10, 0, true);

    // ends of range contained
    assertOverlaps(1, 1, 1, 1, true);
    assertOverlaps(1, 2, 1, 1, true);
    assertOverlaps(1, 2, 2, 2, true);
    assertOverlaps(1, 1, 1, 2, true);
    assertOverlaps(2, 2, 1, 2, true);

    // ends of range overlap
    assertOverlaps(0, 1, 1, 1, true);
    assertOverlaps(1, 1, 0, 1, true);
    assertOverlaps(1, 1, 1, 2, true);
    assertOverlaps(1, 2, 1, 1, true);

    // not overlap touching
    assertOverlaps(1, 1, 0, 0, false);
    assertOverlaps(1, 1, 2, 2, false);
    assertOverlaps(0, 0, 1, 1, false);
    assertOverlaps(2, 2, 1, 1, false);
    assertOverlaps(1, 2, 0, 0, false);
    assertOverlaps(1, 2, 3, 3, false);
    assertOverlaps(0, 0, 1, 2, false);
    assertOverlaps(3, 3, 1, 2, false);
  }
}
