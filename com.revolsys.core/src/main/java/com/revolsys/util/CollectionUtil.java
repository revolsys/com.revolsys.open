package com.revolsys.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class CollectionUtil {
  public static void append(final StringBuffer string,
    final Collection<? extends Object> values) {
    append(string, values, ",");
  }

  public static void append(final StringBuffer string,
    final Collection<? extends Object> values, final String separator) {
    for (final Iterator<? extends Object> iterator = values.iterator(); iterator.hasNext();) {
      final Object value = iterator.next();
      if (value != null) {
        string.append(value);
      }
      if (iterator.hasNext()) {
        string.append(separator);
      }
    }
  }

  public static <T1, T2> Map<T1, T2> createMap(final List<T1> sourceValues,
    final List<T2> targetValues) {
    final Map<T1, T2> map = new HashMap<T1, T2>();
    for (int i = 0; i < sourceValues.size() && i < targetValues.size(); i++) {
      final T1 sourceValue = sourceValues.get(i);
      final T2 targetValue = targetValues.get(i);
      map.put(sourceValue, targetValue);
    }
    return map;
  }

  public static <T> T get(final Collection<T> collection, final int index) {
    int i = 0;
    for (final T object : collection) {
      if (i == index) {
        return object;
      } else {
        i++;
      }
    }
    throw new ArrayIndexOutOfBoundsException(index);
  }

  public static Map<String, Object> toMap(final Preferences preferences) {
    try {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (final String name : preferences.keys()) {
        final Object value = preferences.get(name, null);
        map.put(name, value);
      }
      return map;
    } catch (final BackingStoreException e) {
      throw new RuntimeException("Unable to get preferences " + e);
    }
  }

  public static Map<String, String> toMap(final String string) {
    if (string == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, String> map = new LinkedHashMap<String, String>();
      for (final String entry : string.split("\n")) {
        final String[] pair = entry.split("=");
        if (pair.length == 2) {
          final String name = pair[0];
          final String value = pair[1];
          map.put(name, value);
        } else {
          System.err.println("Invalid entry: " + entry);
        }
      }
      return map;
    }
  }

  /**
   * Convert the collection to a string, using the "," separator between each
   * value. Nulls will be the empty string "".
   * 
   * @param values The values.
   * @param separator The separator.
   * @return The string.
   */
  public static String toString(final Collection<? extends Object> values) {
    return toString(values, ",");
  }

  /**
   * Convert the collection to a string, using the separator between each value.
   * Nulls will be the empty string "".
   * 
   * @param values The values.
   * @param separator The separator.
   * @return The string.
   */
  public static String toString(final Collection<? extends Object> values,
    final String separator) {
    if (values == null) {
      return null;
    } else {
      final StringBuffer string = new StringBuffer();
      append(string, values, separator);
      return string.toString();
    }
  }

  private CollectionUtil() {
  }
}
