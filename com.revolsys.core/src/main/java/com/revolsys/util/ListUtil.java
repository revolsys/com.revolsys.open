package com.revolsys.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtil {
  public static <T> List<T> createList(
    final List<T>... lists) {
    final List<T> newList = new ArrayList<T>();
    for (final List<T> list : lists) {
      newList.addAll(list);
    }
    return newList;
  }

  private ListUtil() {
  }
}
