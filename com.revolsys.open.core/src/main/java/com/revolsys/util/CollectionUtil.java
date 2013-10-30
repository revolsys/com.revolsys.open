package com.revolsys.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;

public final class CollectionUtil {
  public static <V> void addAllIfNotNull(final Collection<V> collection,
    final Collection<V> values) {
    if (collection != null && values != null) {
      collection.addAll(values);
    }

  }

  public static <T> Integer addCount(final Map<T, Integer> counts,
    final T object) {
    Integer count = counts.get(object);
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    counts.put(object, count);
    return count;
  }

  public static <V> boolean addIfNotNull(final Collection<V> collection,
    final V value) {
    if (value == null) {
      return false;
    } else {
      return collection.add(value);
    }
  }

  public static <K1, V> boolean addToList(final Map<K1, List<V>> map,
    final K1 key1, final V value) {
    final List<V> values = getList(map, key1);
    return values.add(value);
  }

  public static <K1, K2, V> void addToMap(final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2, final V value) {
    final Map<K2, V> mapValue = getMap(map, key1);
    mapValue.put(key2, value);
  }

  public static <K1, V> boolean addToSet(final Map<K1, Set<V>> map,
    final K1 key1, final V value) {
    final Set<V> values = getSet(map, key1);
    return values.add(value);
  }

  public static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map,
    final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, key1);
    return values.add(value);
  }

  public static void append(final StringBuffer string,
    final Collection<? extends Object> values) {
    append(string, values, ",");
  }

  public static void append(final StringBuffer buffer,
    final Collection<? extends Object> values, final String separator) {
    boolean first = true;
    for (final Object value : values) {
      if (value != null) {
        final String string = StringConverterRegistry.toString(value);
        if (StringUtils.hasText(string)) {
          if (first) {
            first = false;
          } else {
            buffer.append(separator);
          }
          buffer.append(string);
        }
      }
    }
  }

  public static List<? extends Object> arrayToList(final Object value) {
    final List<Object> list = new ArrayList<Object>();
    if (value instanceof boolean[]) {
      for (final Object item : (boolean[])value) {
        list.add(item);
      }
    } else if (value instanceof Object[]) {
      for (final Object item : (Object[])value) {
        list.add(item);
      }
    } else if (value instanceof byte[]) {
      for (final Object item : (byte[])value) {
        list.add(item);
      }
    } else if (value instanceof short[]) {
      for (final Object item : (short[])value) {
        list.add(item);
      }
    } else if (value instanceof int[]) {
      for (final Object item : (int[])value) {
        list.add(item);
      }
    } else if (value instanceof long[]) {
      for (final Object item : (long[])value) {
        list.add(item);
      }
    } else if (value instanceof float[]) {
      for (final Object item : (float[])value) {
        list.add(item);
      }
    } else if (value instanceof double[]) {
      for (final Object item : (double[])value) {
        list.add(item);
      }
    } else {
      list.add(value);
    }
    return list;
  }

  public static <T> boolean containsReference(
    final List<WeakReference<T>> list, final T object) {
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (value == object) {
        return true;
      }
    }
    return false;
  }

  public static <K, V> Map<K, V> createHashMap(final Map<K, ? extends V> map) {
    final Map<K, V> copy = new HashMap<K, V>();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  public static <V> Set<V> createHashSet(final Collection<? extends V> set) {
    final Set<V> copy = new HashSet<V>();
    if (set != null) {
      copy.addAll(set);
    }
    return copy;

  }

  public static <K, V> Map<K, V> createLinkedHashMap(
    final Map<K, ? extends V> map) {
    final Map<K, V> copy = new LinkedHashMap<K, V>();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  public static <V> Set<V> createLinkedHashSet(final Collection<? extends V> set) {
    final Set<V> copy = new LinkedHashSet<V>();
    if (set != null) {
      copy.addAll(set);
    }
    return copy;

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

  /**
   * Get the value for the key from the map. If the value was null return
   * default Value instead.
   * 
   * @param map The map.
   * @param key The key to return the value for.
   * @param defaultValue The default value.
   * @return The value.
   */
  public static <T> T get(final Map<?, ?> map, final Object key,
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

  public static boolean getBool(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  public static Boolean getBoolean(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.valueOf(value.toString());
    }
  }

  public static Double getDouble(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String stringValue = value.toString();
      if (StringUtils.hasText(stringValue)) {
        try {
          return Double.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  public static Double getDouble(final Map<String, ? extends Object> object,
    final String name, final Double defaultValue) {
    final Double value = getDouble(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static Double getDoubleValue(final Map<String, ? extends Object> map,
    final String name) {
    final Number value = (Number)map.get(name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  public static Integer getInteger(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String stringValue = value.toString();
      if (StringUtils.hasText(stringValue)) {
        try {
          return Integer.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  public static Integer getInteger(final Map<String, ? extends Object> object,
    final String name, final Integer defaultValue) {
    final Integer value = getInteger(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static <K, V> List<V> getList(final Map<K, List<V>> map, final K key) {
    List<V> value = map.get(key);
    if (value == null) {
      value = new ArrayList<V>();
      map.put(key, value);
    }
    return value;
  }

  public static Long getLong(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String stringValue = value.toString();
      if (StringUtils.hasText(stringValue)) {
        try {
          return Long.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  public static <K1, K2, V> Map<K2, V> getMap(final Map<K1, Map<K2, V>> map,
    final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = new LinkedHashMap<K2, V>();
      map.put(key, value);
    }
    return value;
  }

  public static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2) {
    final Map<K2, V> values = getMap(map, key1);
    return values.get(key2);
  }

  public static <K, V> List<V> getNotNull(final Map<K, V> map,
    final Collection<K> keys) {
    final List<V> values = new ArrayList<V>();
    if (keys != null) {
      for (final K key : keys) {
        final V value = map.get(key);
        if (value != null) {
          values.add(value);
        }
      }
    }
    return values;
  }

  public static <T> List<T> getReferences(final List<WeakReference<T>> list) {
    final List<T> values = new ArrayList<T>();
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else {
        values.add(value);
      }
    }
    return values;
  }

  public static <K, V> Set<V> getSet(final Map<K, Set<V>> map, final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new LinkedHashSet<V>();
      map.put(key, value);
    }
    return value;
  }

  public static String getString(final Map<String, ? extends Object> map,
    final String name) {
    final Object value = map.get(name);
    if (value == null) {
      return null;
    } else {
      return StringConverterRegistry.toString(value);
    }
  }

  public static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new TreeSet<V>();
      map.put(key, value);
    }
    return value;
  }

  public static boolean isNotNullAndNotZero(final Map<String, Object> object,
    final String name) {
    final Integer value = getInteger(object, name);
    if (value == null || value == 0) {
      return false;
    } else {
      return true;
    }
  }

  public static <K1, K2, V> V put(final Map<K1, Map<K2, V>> map, final K1 key1,
    final K2 key2, final V value) {
    final Map<K2, V> values = getMap(map, key1);
    return values.put(key2, value);
  }

  public static <K, V extends Comparable<V>> void putIfGreaterThan(
    final Map<K, V> map, final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) > 1) {
        map.put(key, value);
      }
    }
  }

  public static <K, V> boolean removeFromSet(final Map<K, Set<V>> map,
    final K key, final V value) {
    final Set<V> values = map.get(key);
    if (values == null) {
      return false;
    } else {
      final boolean removed = values.remove(value);
      if (values.isEmpty()) {
        map.remove(key);
      }
      return removed;
    }
  }

  public static <K, V extends Comparable<V>> void removeIfGreaterThanEqual(
    final Map<K, V> map, final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) >= 0) {
        map.remove(key);
      }
    }
  }

  public static <K, V extends Comparable<V>> void removeIfLessThanEqual(
    final Map<K, V> map, final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) <= 0) {
        map.remove(key);
      }
    }
  }

  public static <T> void removeReference(final List<WeakReference<T>> list,
    final T object) {
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (value == object) {
        list.remove(i);
      }
    }
  }

  public static final String replaceProperties(final CharSequence string,
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

  public static List<String> split(final String text, final String regex) {
    if (StringUtils.hasText(text)) {
      return Arrays.asList(text.split(regex));
    } else {
      return Collections.emptyList();
    }
  }

  public static <T> List<T> subList(final Iterable<T> iterable, final int size) {
    final List<T> list = new ArrayList<T>(size);
    int i = 0;
    for (final T value : iterable) {
      if (i < size) {
        list.add(value);
        i++;
      } else {
        return list;
      }
    }
    return list;
  }

  public static float[] toFloatArray(final double[] doubleArray) {
    if (doubleArray == null) {
      return null;
    } else {
      final int size = doubleArray.length;
      final float[] floatArray = new float[size];
      for (int i = 0; i < size; i++) {
        floatArray[i] = (float)doubleArray[i];
      }
      return floatArray;
    }
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
  public static String toString(final String separator,
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
