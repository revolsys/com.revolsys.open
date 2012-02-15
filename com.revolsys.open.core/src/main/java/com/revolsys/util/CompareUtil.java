package com.revolsys.util;

public class CompareUtil {
  public static <T> int compare(final Comparable<T> object1, final T object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (object2 == null) {
      return 1;
    } else {
      return object1.compareTo(object2);
    }
  }
}
