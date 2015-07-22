package com.revolsys.jts.geom;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum End {
  FROM, TO;

  public static List<End> VALUES = Lists.array(FROM, TO);

  public static boolean isFrom(final End end) {
    return end == FROM;
  }

  public static boolean isTo(final End end) {
    return end == TO;
  }

  public static End opposite(final End end) {
    if (end == FROM) {
      return TO;
    } else if (end == TO) {
      return FROM;
    } else {
      return null;
    }
  }
}
