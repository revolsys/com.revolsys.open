package com.revolsys.jts.geom;

public enum LineEnd {
  FROM, TO;

  public static boolean isFrom(final LineEnd lineEnd) {
    return lineEnd == FROM;
  }

  public static boolean isTo(final LineEnd lineEnd) {
    return lineEnd == TO;
  }
}
