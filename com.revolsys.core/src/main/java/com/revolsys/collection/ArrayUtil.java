package com.revolsys.collection;

import java.util.List;

public class ArrayUtil {
  public static <T> T[] create(
    final List<T> list) {
    if (list == null) {
      return null;
    } else {
      final T[] array = (T[])new Object[list.size()];
      return list.toArray(array);
    }
  }

  public static <T> T[] create(
    final T... o) {
    return o;
  }

  public static int[] createDoubleArray(
    final List<Integer> list) {
    if (list == null) {
      return null;
    } else {
      final int[] array = new int[list.size()];
      for (int i = 0; i < array.length; i++) {
        array[i] = list.get(i);
      }
      return array;
    }
  }

  public static int[] createIntArray(
    final List<Integer> list) {
    if (list == null) {
      return null;
    } else {
      final int[] array = new int[list.size()];
      for (int i = 0; i < array.length; i++) {
        array[i] = list.get(i);
      }
      return array;
    }
  }
}
