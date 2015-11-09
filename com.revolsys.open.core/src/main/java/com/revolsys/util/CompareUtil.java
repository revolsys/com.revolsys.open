package com.revolsys.util;

import java.util.Comparator;

import com.revolsys.converter.string.StringConverter;

public class CompareUtil {
  public static <T> int compare(final Comparable<T> object1, T object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (object2 == null) {
      return 1;
    } else {
      if (object1 instanceof Number) {
        object2 = StringConverter.toObject(object1.getClass(), object2);
      }
      return object1.compareTo(object2);
    }
  }

  public static <T> int compare(final Comparable<T> object1, T object2, final boolean nullsFirst) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        if (nullsFirst) {
          return -1;
        } else {
          return 1;
        }
      }
    } else if (object2 == null) {
      if (nullsFirst) {
        return 1;
      } else {
        return -1;
      }
    } else {
      if (object1 instanceof Number) {
        object2 = StringConverter.toObject(object1.getClass(), object2);
      }
      return object1.compareTo(object2);
    }
  }

  public static <T> int compare(final Comparator<T> comparator, final T object1, final T object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (object2 == null) {
      return 1;
    } else {
      return comparator.compare(object1, object2);
    }
  }

  public static int compare(final Object object1, Object object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (object2 == null) {
      return 1;
    } else if (object1 instanceof Comparable) {
      if (object1 instanceof Number) {
        object2 = StringConverter.toObject(object1.getClass(), object2);
      } else if (object2 instanceof Number) {
        object2 = StringConverter.toString(object2);
      }
      @SuppressWarnings("unchecked")
      final Comparable<Object> comparable = (Comparable<Object>)object1;
      return comparable.compareTo(object2);
    } else {
      return object1.toString().compareTo(object2.toString());
    }
  }
}
