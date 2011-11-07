package com.revolsys.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class SetUtil {
  public static <T> Set<T> createSet(
    final Collection<T>... collections) {
    final Set<T> newSet = new LinkedHashSet<T>();
    for (final Collection<T> list : collections) {
      newSet.addAll(list);
    }
    return newSet;
  }

  private SetUtil() {
  }
}
