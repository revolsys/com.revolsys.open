package com.revolsys.util;

public class HexConverter {
  public static String toHex(final String string) {
    final StringBuffer buf = new StringBuffer();
    for (final char c : string.toCharArray()) {
      buf.append(Integer.toHexString(c));
    }
    return buf.toString();
  }
}
