package com.revolsys.jts.util;

public class NumberUtil {

  public static boolean equalsWithTolerance(final double x1, final double x2, final double tolerance) {
    return Math.abs(x1 - x2) <= tolerance;
  }

  public static boolean isInteger(final String part) {
    try {
      Integer.parseInt(part);
      return true;
    } catch (final NumberFormatException e) {
      return false;
    }
  }

}
