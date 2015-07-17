package com.revolsys.jts.geom;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum LineEnd {
  FROM, TO;

  public static List<LineEnd> VALUES = Lists.array(FROM, TO);

  public static boolean isFrom(final LineEnd lineEnd) {
    return lineEnd == FROM;
  }

  public static boolean isTo(final LineEnd lineEnd) {
    return lineEnd == TO;
  }

  public static LineEnd opposite(final LineEnd lineEnd) {
    if (lineEnd == FROM) {
      return TO;
    } else if (lineEnd == TO) {
      return FROM;
    } else {
      return null;
    }
  }
}
