package com.revolsys.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.factory.Factory;

public class Maps {

  public static <T> Integer addCount(final Map<T, Integer> counts, final T object) {
    Integer count = counts.get(object);
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    counts.put(object, count);
    return count;
  }

  public static <K1, V> boolean addToList(final Map<K1, List<V>> map, final K1 key1, final V value) {
    final List<V> values = getList(map, key1);
    return values.add(value);
  }

  public static <K1, K2, V> void addToMap(final Map<K1, Map<K2, V>> map, final K1 key1,
    final K2 key2, final V value) {
    final Map<K2, V> mapValue = getMap(map, key1);
    mapValue.put(key2, value);
  }

  public static <K1, V> boolean addToSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = CollectionUtil.getSet(map, key1);
    return values.add(value);
  }

  public static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map,
    final Comparator<V> comparator, final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, comparator, key1);
    return values.add(value);
  }

  public static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, key1);
    if (values == null) {
      return false;
    } else {
      return values.add(value);
    }
  }

  public static <K1, K2, V> boolean containsKey(final Map<K1, Map<K2, V>> map, final K1 key1,
    final K2 key2) {
    final Map<K2, V> mapValue = getMap(map, key1);
    return mapValue.containsKey(key2);
  }

  public static <K, V> Map<K, V> create(final K key, final V value) {
    final Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    return map;
  }

  public static <K, V> Map<K, V> createHashMap(final Map<K, ? extends V> map) {
    final Map<K, V> copy = new HashMap<K, V>();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  public static <K, V> Map<K, V> createLinkedHashMap(final Map<K, ? extends V> map) {
    final Map<K, V> copy = new LinkedHashMap<K, V>();
    if (map != null) {
      copy.putAll(map);
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

  /**
   * Get the value for the key from the map. If the value was null return
   * default Value instead.
   *
   * @param map The map.
   * @param key The key to return the value for.
   * @param defaultValue The default value.
   * @return The value.
   */
  public static <T> T get(final Map<?, ?> map, final Object key, final T defaultValue) {
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

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static <K, V> V get(final Map<K, ? extends Object> map, final K key,
    final Factory<V> factory) {
    V value = (V)map.get(key);
    if (value == null) {
      value = factory.create();
      ((Map)map).put(key, value);
    }
    return value;
  }

  public static Object get(final Map<String, ? extends Object> map, final String name) {
    if (map == null) {
      return null;
    } else {
      return map.get(name);
    }
  }

  public static boolean getBool(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  public static boolean getBool(final Map<String, ? extends Object> map, final String name,
    final boolean defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  public static Boolean getBoolean(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.valueOf(value.toString());
    }
  }

  public static Double getDouble(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
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

  public static double getDouble(final Map<String, ? extends Object> object, final String name,
    final double defaultValue) {
    final Double value = getDouble(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static Double getDoubleValue(final Map<String, ? extends Object> map, final String name) {
    final Number value = (Number)get(map, name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  public static Integer getInteger(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
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

  public static int getInteger(final Map<String, ? extends Object> object, final String name,
    final int defaultValue) {
    final Integer value = getInteger(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static <K, V> List<V> getList(final Map<K, List<V>> map, final K key) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
      map.put(key, list);
    }
    return list;
  }

  public static Long getLong(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
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

  public static long getLong(final Map<String, ? extends Object> map, final String name,
    final long defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
        try {
          return Long.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          throw new IllegalArgumentException(value + " is not a valid long");
        }
      } else {
        return defaultValue;
      }
    }
  }

  public static <K1, K2, V> Map<K2, V> getMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = new LinkedHashMap<K2, V>();
      map.put(key, value);
    }
    return value;
  }

  public static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2) {
    final Map<K2, V> values = getMap(map, key1);
    return values.get(key2);
  }

  public static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final Factory<V> factory) {
    final Map<K2, V> values = getMap(map, key1);
    return get(values, key2, factory);
  }

  public static <K, V> List<V> getNotNull(final Map<K, V> map, final Collection<K> keys) {
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

  public static String getString(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else {
      return StringConverterRegistry.toString(value);
    }
  }

  public static String getString(final Map<String, ? extends Object> map, final String name,
    final String defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else {
      return StringConverterRegistry.toString(value);
    }
  }

  public static <K1, K2, V> Map<K2, V> getTreeMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = new TreeMap<K2, V>();
      map.put(key, value);
    }
    return value;
  }

  public static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final Comparator<V> comparator,
    final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new TreeSet<V>(comparator);
      map.put(key, value);
    }
    return value;
  }

  public static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final K key) {
    if (key == null) {
      return null;
    } else {
      Set<V> value = map.get(key);
      if (value == null) {
        value = new TreeSet<V>();
        map.put(key, value);
      }
      return value;
    }
  }

  public static <K, V> Map<K, V> hashMap(final K key, final V value) {
    final Map<K, V> map = new HashMap<>();
    map.put(key, value);
    return map;
  }

  public static boolean isNotNullAndNotZero(final Map<String, Object> object, final String name) {
    final Integer value = getInteger(object, name);
    if (value == null || value == 0) {
      return false;
    } else {
      return true;
    }
  }

  public static <K, V> void mergeCollection(final Map<K, Collection<V>> map,
    final Map<K, Collection<V>> otherMap) {
    for (final Entry<K, Collection<V>> entry : otherMap.entrySet()) {
      final K key = entry.getKey();
      Collection<V> collection = map.get(key);
      final Collection<V> otherCollection = otherMap.get(key);
      if (collection == null) {
        collection = JavaBeanUtil.clone(otherCollection);
        map.put(key, collection);
      } else {
        for (final V value : otherCollection) {
          if (!collection.contains(value)) {
            collection.add(value);
          }
        }
      }
    }
  }

  public static <K1, K2, V> V put(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V value) {
    final Map<K2, V> values = getMap(map, key1);
    return values.put(key2, value);
  }

  public static <K, V extends Comparable<V>> void putIfGreaterThan(final Map<K, V> map,
    final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) > 1) {
        map.put(key, value);
      }
    }
  }

  public static <K, V> boolean removeFromCollection(final Map<K, ? extends Collection<V>> map,
    final K key, final V value) {
    final Collection<V> values = map.get(key);
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

  public static <K, V> boolean removeFromSet(final Map<K, Set<V>> map, final K key, final V value) {
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

  public static <K, V extends Comparable<V>> void removeIfGreaterThanEqual(final Map<K, V> map,
    final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) >= 0) {
        map.remove(key);
      }
    }
  }

  public static <K, V extends Comparable<V>> void removeIfLessThanEqual(final Map<K, V> map,
    final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) <= 0) {
        map.remove(key);
      }
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

  public static <K, V> Map<K, V> treeMap(final K key, final V value) {
    final Map<K, V> map = new TreeMap<>();
    map.put(key, value);
    return map;
  }

}
