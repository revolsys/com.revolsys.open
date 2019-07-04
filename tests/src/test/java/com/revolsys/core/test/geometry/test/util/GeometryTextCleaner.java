package com.revolsys.core.test.geometry.test.util;

/**
 * Cleans text strings which are supposed
 * to contain valid text for Geometries
 * (either WKB, WKB, or GML)
 *
 * @author mbdavis
 *
 */
public class GeometryTextCleaner {
  public static final String WKT_SYMBOLS = "(),.-";

  private static String clean(final String input, final String allowedSymbols) {
    final StringBuilder buf = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (isAllowed(c, allowedSymbols)) {
        buf.append(c);
      }
    }
    return buf.toString();
  }

  public static String cleanWKT(final String input) {
    return clean(input, WKT_SYMBOLS);
  }

  private static boolean isAllowed(final char c, final String allowedSymbols) {
    if (Character.isWhitespace(c)) {
      return true;
    }
    if (Character.isLetterOrDigit(c)) {
      return true;
    }
    if (allowedSymbols.indexOf(c) >= 0) {
      return true;
    }
    return false;
  }

}
