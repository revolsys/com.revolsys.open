package com.revolsys.collection.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public interface Maps {
  static <K1, V> boolean addAllToSet(final Map<K1, Set<V>> map, final K1 key1,
    final Collection<? extends V> values) {
    if (Property.hasValue(values)) {
      final Set<V> set = getSet(map, key1);
      return set.addAll(values);
    } else {
      return true;
    }
  }

  static <T> Integer addCount(final Map<T, Integer> counts, final T key) {
    Integer count = counts.get(key);
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    counts.put(key, count);
    return count;
  }

  static <K, V, C extends Collection<V>> boolean addToCollection(final Supplier<C> supplier,
    final Map<K, C> map, final K key, final V value) {
    final C values = get(map, key, supplier);
    return values.add(value);
  }

  static <K1, V> boolean addToList(final Map<K1, List<V>> map, final K1 key1, final V value) {
    if (map != null && key1 != null) {
      final List<V> values = getList(map, key1);
      return values.add(value);
    } else {
      return false;
    }
  }

  static <K1, K2, V> boolean addToList(final Map<K1, Map<K2, List<V>>> map, final K1 key1,
    final K2 key2, final V value) {
    final List<V> values = getList(map, key1, key2);
    return values.add(value);
  }

  static <K1, K2, V> boolean addToList(final Supplier<Map<K2, List<V>>> supplier,
    final Map<K1, Map<K2, List<V>>> map, final K1 key1, final K2 key2, final V value) {
    final List<V> values = getList(supplier, map, key1, key2);
    return values.add(value);
  }

  static <K1, K2, V> V addToMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V value) {
    final Map<K2, V> mapValue = getMap(map, key1);
    return mapValue.put(key2, value);
  }

  static <K1, K2, V> V addToMap(final Supplier<Map<K2, V>> supplier, final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2, final V value) {
    final Map<K2, V> mapValue = getMap(supplier, map, key1);
    return mapValue.put(key2, value);
  }

  static <K1, V> boolean addToSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = getSet(map, key1);
    return values.add(value);
  }

  static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map, final Comparator<V> comparator,
    final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, comparator, key1);
    return values.add(value);
  }

  static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, key1);
    if (values == null) {
      return false;
    } else {
      return values.add(value);
    }
  }

  static <K, V> MapBuilder<K, V> buildLinkedHash() {
    final Map<K, V> map = newLinkedHash();
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildLinkedHash(final Map<K, V> values) {
    final Map<K, V> map = newLinkedHash(values);
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildTree() {
    final Map<K, V> map = newTree();
    return new MapBuilder<>(map);
  }

  static <K, V> boolean collectionContains(final Map<K, ? extends Collection<? extends V>> map,
    final K key, final V value) {
    if (map == null || key == null) {
      return false;
    } else {
      final Collection<? extends V> collection = map.get(key);
      if (collection == null) {
        return false;
      } else {
        return collection.contains(value);
      }
    }
  }

  static <K1, V> boolean containsInCollection(final Map<K1, ? extends Collection<V>> map,
    final K1 key, final V value) {
    final Collection<V> collection = map.get(key);
    if (collection == null) {
      return false;
    } else {
      return collection.contains(value);
    }
  }

  static <K1, K2, V> boolean containsKey(final Map<K1, Map<K2, V>> map, final K1 key1,
    final K2 key2) {
    final Map<K2, V> mapValue = getMap(map, key1);
    return mapValue.containsKey(key2);
  }

  static <T> Integer decrementCount(final Map<T, Integer> counts, final T key) {
    Integer count = counts.get(key);
    if (count == null) {
      return 0;
    } else {
      count--;
      if (count <= 0) {
        counts.remove(key);
      } else {
        counts.put(key, count);
      }
      return count;
    }
  }

  static <V> V first(final Map<?, V> map) {
    if (Property.hasValue(map)) {
      return map.values().iterator().next();
    }
    return null;
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
  static <T> T get(final Map<?, ?> map, final Object key, final T defaultValue) {
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

  @SuppressWarnings("unchecked")
  static <K, V> V get(final Map<K, ? extends Object> map, final K key) {
    if (map == null) {
      return null;
    } else {
      return (V)map.get(key);
    }
  }

  static <K, V> V get(final Map<K, V> map, final K key, final Function<K, V> defaultFactory) {
    V value = map.get(key);
    if (value == null) {
      value = defaultFactory.apply(key);
      map.put(key, value);
    }
    return value;
  }

  static <K, V> V get(final Map<K, V> map, final K key, final Supplier<V> defaultFactory) {
    V value = map.get(key);
    if (value == null) {
      value = defaultFactory.get();
      map.put(key, value);
    }
    return value;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <K, V> V get(final Supplier<V> supplier, final Map<K, ? extends Object> map, final K key) {
    V value = (V)map.get(key);
    if (value == null) {
      value = supplier.get();
      ((Map)map).put(key, value);
    }
    return value;
  }

  static boolean getBool(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  static boolean getBool(final Map<String, ? extends Object> map, final String name,
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

  static Boolean getBoolean(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.valueOf(value.toString());
    }
  }

  static <T> Integer getCount(final Map<T, Integer> counts, final T key) {
    final Integer count = counts.get(key);
    if (count == null) {
      return 0;
    } else {
      return count;
    }
  }

  static Double getDouble(final Map<String, ? extends Object> map, final String name) {
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

  static double getDouble(final Map<String, ? extends Object> object, final String name,
    final double defaultValue) {
    final Double value = getDouble(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  static Double getDoubleValue(final Map<String, ? extends Object> map, final String name) {
    final Number value = (Number)get(map, name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  static Integer getInteger(final Map<String, ? extends Object> map, final String name) {
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

  static int getInteger(final Map<String, ? extends Object> object, final String name,
    final int defaultValue) {
    final Integer value = getInteger(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  static <K, V> List<V> getList(final Map<K, List<V>> map, final K key) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
      map.put(key, list);
    }
    return list;
  }

  static <K1, K2, V> List<V> getList(final Map<K1, Map<K2, List<V>>> map, final K1 key1,
    final K2 key2) {
    final Map<K2, List<V>> map2 = getMap(map, key1);
    final List<V> list = getList(map2, key2);
    return list;
  }

  static <K1, K2, V> List<V> getList(final Supplier<Map<K2, List<V>>> supplier,
    final Map<K1, Map<K2, List<V>>> map, final K1 key1, final K2 key2) {
    final Map<K2, List<V>> map2 = getMap(supplier, map, key1);
    final List<V> list = getList(map2, key2);
    return list;
  }

  static Long getLong(final Map<String, ? extends Object> map, final String name) {
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

  static long getLong(final Map<String, ? extends Object> map, final String name,
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

  static <K1, K2, V> Map<K2, V> getMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = newLinkedHash();
      map.put(key, value);
    }
    return value;
  }

  static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2) {
    final Map<K2, V> values = getMap(map, key1);
    return values.get(key2);
  }

  static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final Supplier<V> supplier) {
    final Map<K2, V> values = getMap(map, key1);
    return get(supplier, values, key2);
  }

  static <K1, K2, V> Map<K2, V> getMap(final Supplier<Map<K2, V>> supplier,
    final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = supplier.get();
      map.put(key, value);
    }
    return value;
  }

  static <K, V> List<V> getNotNull(final Map<K, V> map, final Collection<K> keys) {
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

  static <K, V> Set<V> getSet(final Map<K, Set<V>> map, final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new LinkedHashSet<V>();
      map.put(key, value);
    }
    return value;
  }

  static String getString(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else {
      return StringConverter.toString(value);
    }
  }

  static String getString(final Map<String, ? extends Object> map, final String name,
    final String defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else {
      return StringConverter.toString(value);
    }
  }

  static <K1, K2, V> Map<K2, V> getTreeMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = newTree();
      map.put(key, value);
    }
    return value;
  }

  static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final Comparator<V> comparator,
    final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new TreeSet<V>(comparator);
      map.put(key, value);
    }
    return value;
  }

  static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final K key) {
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

  static <K, V> Supplier<Map<K, V>> hashFactory() {
    return () -> {
      return newHash();
    };
  }

  static <K> boolean hasValue(final Map<K, ?> map, final K key) {
    if (map == null || key == null) {
      return false;
    } else {
      final Object value = map.get(key);
      return Property.hasValue(value);
    }
  }

  static boolean isNotNullAndNotZero(final Map<String, Object> object, final String name) {
    final Integer value = getInteger(object, name);
    if (value == null || value == 0) {
      return false;
    } else {
      return true;
    }
  }

  static <K, V> Supplier<Map<K, V>> linkedHashFactory() {
    return () -> {
      return newLinkedHash();
    };
  }

  static <K, V> void mergeCollection(final Map<K, Collection<V>> map,
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

  static <V, K> HashMap<K, V> newHash() {
    return new HashMap<K, V>();
  }

  static <K, V> Map<K, V> newHash(final K key, final V value) {
    final Map<K, V> map = newHash();
    map.put(key, value);
    return map;
  }

  static <K, V> Map<K, V> newHash(final Map<K, ? extends V> map) {
    final Map<K, V> copy = newHash();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  static <K, V> LinkedHashMap<K, V> newLinkedHash() {
    return new LinkedHashMap<>();
  }

  static <K, V> Map<K, V> newLinkedHash(final K key, final V value) {
    final Map<K, V> map = newLinkedHash();
    map.put(key, value);
    return map;
  }

  static <T1, T2> Map<T1, T2> newLinkedHash(final List<T1> sourceValues,
    final List<T2> targetValues) {
    final Map<T1, T2> map = newLinkedHash();
    for (int i = 0; i < sourceValues.size() && i < targetValues.size(); i++) {
      final T1 sourceValue = sourceValues.get(i);
      final T2 targetValue = targetValues.get(i);
      map.put(sourceValue, targetValue);
    }
    return map;
  }

  static <K, V> Map<K, V> newLinkedHash(final Map<K, ? extends V> map) {
    final Map<K, V> copy = newLinkedHash();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  static <K, V> Map<K, V> newTree() {
    return new TreeMap<>();
  }

  static <K extends Comparable<K>, V extends Comparable<V>> TreeMap<K, V> newTree(
    final Comparator<K> comparator) {
    return new TreeMap<>(comparator);
  }

  static <K, V> Map<K, V> newTree(final Comparator<K> comparator, final Map<K, ? extends V> map) {
    final Map<K, V> newMap = newTree();
    if (map != null) {
      newMap.putAll(map);
    }
    return newMap;
  }

  static <K, V> Map<K, V> newTree(final K key, final V value) {
    final Map<K, V> map = newTree();
    map.put(key, value);
    return map;
  }

  static <K, V> Map<K, V> newTree(final Map<K, ? extends V> map) {
    final Map<K, V> newMap = newTree();
    if (map != null) {
      newMap.putAll(map);
    }
    return newMap;
  }

  static <K1, K2, V> V put(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V value) {
    final Map<K2, V> values = getMap(map, key1);
    return values.put(key2, value);
  }

  static <K1, K2, V> V put(final Supplier<Map<K2, V>> factory, final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2, final V value) {
    final Map<K2, V> values = getMap(factory, map, key1);
    return values.put(key2, value);
  }

  static <K, V extends Comparable<V>> void putIfGreaterThan(final Map<K, V> map, final K key,
    final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) > 1) {
        map.put(key, value);
      }
    }
  }

  static <K, V> boolean removeFromCollection(final Map<K, ? extends Collection<V>> map, final K key,
    final V value) {
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

  static <K, V> boolean removeFromSet(final Map<K, Set<V>> map, final K key, final V value) {
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

  static <K, V extends Comparable<V>> void removeIfGreaterThanEqual(final Map<K, V> map,
    final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) >= 0) {
        map.remove(key);
      }
    }
  }

  static <K, V extends Comparable<V>> void removeIfLessThanEqual(final Map<K, V> map, final K key,
    final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) <= 0) {
        map.remove(key);
      }
    }
  }

  static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValues(
    final Map<K, V> map) {
    final MapValueComparator<K, V> comparator = new MapValueComparator<K, V>(map);
    final Map<K, V> sortedMap = newTree(comparator);
    sortedMap.putAll(map);
    return newLinkedHash(sortedMap);
  }

  static Map<String, Object> toMap(final Preferences preferences) {
    try {
      final Map<String, Object> map = newHash();
      for (final String name : preferences.keys()) {
        final Object value = preferences.get(name, null);
        map.put(name, value);
      }
      return map;
    } catch (final BackingStoreException e) {
      throw new RuntimeException("Unable to get preferences " + e);
    }
  }

  static Map<String, String> toMap(final String string) {
    if (string == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, String> map = newLinkedHash();
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

  static <K, V> Supplier<Map<K, V>> treeFactory() {
    return () -> {
      return newTree();
    };
  }
}
