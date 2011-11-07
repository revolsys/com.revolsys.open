package com.revolsys.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public class ArrayUtil {
  public static <T> T[] create(
    final Collection<T> list) {
    if (list == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final T[] array = (T[])new Object[list.size()];
      return list.toArray(array);
    }
  }

  public static <T> T[] create(
    final T... o) {
    return o;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] create(Class<T> clazz,int size) {
    return (T[])Array.newInstance(clazz,size);
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
