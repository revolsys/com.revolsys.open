package com.revolsys.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class CollectionUtil {
  public static void append(
    final StringBuffer string,
    final Collection<? extends Object> values) {
    append(string, values, ",");
  }

  public static void append(
    final StringBuffer string,
    final Collection<? extends Object> values,
    final String separator) {
    boolean first = true;
    for (final Object value : values) {
      if (value != null) {
        if (first) {
          first = false;
        } else {
          string.append(separator);
        }
        string.append(value);
      }
    }
  }

  public static <T1, T2> Map<T1, T2> createMap(
    final List<T1> sourceValues,
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

  /**
   * Get the value for the key from the map. If the value was null return
   * default Value instead.
   * 
   * @param map The map.
   * @param key The key to return the value for.
   * @param defaultValue The default value.
   * @return The value.
   */
  public static <T> T get(
    final Map<?, ?> map,
    final Object key,
    final T defaultValue) {
    if (map == null) {
      return defaultValue;
    } else {
      @SuppressWarnings("unchecked")
      final T value = (T)map.get(key);
      if (value == null) {
        return defaultValue;
      } else {
        return value;
      }
    }
  }

  public static final String replaceProperties(
    final CharSequence string,
    final Map<String, Object> properties) {
    if (string == null) {
      return null;
    } else {
      final StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < string.length(); ++i) {
        char c = string.charAt(i);
        switch (c) {
          case '$':
            ++i;
            if (i < string.length()) {
              c = string.charAt(i);
              if (c == '{') {
                ++i;
                final StringBuffer propertyName = new StringBuffer();
                for (; i < string.length() && c != '}'; ++i) {
                  c = string.charAt(i);
                  if (c != '}') {
                    propertyName.append(c);
                  }
                }
                Object value = null;
                if (propertyName.length() > 0) {
                  value = properties.get(propertyName.toString());

                }
                if (value == null) {
                  buffer.append("${");
                  buffer.append(propertyName);
                  buffer.append("}");
                } else {
                  buffer.append(value);
                }
              }
            }
          break;

          default:
            buffer.append(c);
          break;
        }
      }
      return buffer.toString();
    }
  }

  public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValues(
    final Map<K, V> map) {
    final MapValueComparator<K, V> comparator = new MapValueComparator<K, V>(
      map);
    final Map<K, V> sortedMap = new TreeMap<K, V>(comparator);
    sortedMap.putAll(map);
    return new LinkedHashMap<K, V>(sortedMap);
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
    return toString(",", values);
  }

  /**
   * Convert the collection to a string, using the separator between each value.
   * Nulls will be the empty string "".
   * 
   * @param separator The separator.
   * @param values The values.
   * @return The string.
   */
  public static String toString(
    final String separator,
    final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuffer string = new StringBuffer();
      append(string, values, separator);
      return string.toString();
    }
  }

  public static String toString(final String separator, final Object... values) {
    return toString(separator, Arrays.asList(values));
  }

  private CollectionUtil() {
  }
}
