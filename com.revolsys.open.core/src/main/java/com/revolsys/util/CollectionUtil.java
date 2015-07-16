package com.revolsys.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.revolsys.converter.string.StringConverterRegistry;

public final class CollectionUtil {
  public static <V> void addAllIfNotNull(final Collection<V> collection,
    final Collection<V> values) {
    if (collection != null && values != null) {
      collection.addAll(values);
    }

  }

  public static <V> boolean addIfNotNull(final Collection<V> collection, final V value) {
    if (value == null) {
      return false;
    } else {
      return collection.add(value);
    }
  }

  public static void append(final StringBuilder string, final Collection<? extends Object> values) {
    append(string, values, ",");
  }

  public static void append(final StringBuilder buffer, final Collection<? extends Object> values,
    final boolean skipNulls, final String separator) {
    boolean first = true;
    for (final Object value : values) {
      final String string = StringConverterRegistry.toString(value);
      if (!skipNulls || Property.hasValue(string)) {
        if (first) {
          first = false;
        } else {
          buffer.append(separator);
        }
        if (string != null) {
          buffer.append(string);
        }
      }
    }
  }

  public static void append(final StringBuilder buffer, final Collection<? extends Object> values,
    final String separator) {
    boolean first = true;
    for (final Object value : values) {
      if (value != null) {
        final String string = StringConverterRegistry.toString(value);
        if (Property.hasValue(string)) {
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

  public static <V> boolean collectionContains(final Map<Object, Collection<V>> map,
    final Object key, final V value) {
    if (map == null) {
      return false;
    } else {
      final Collection<V> collection = map.get(key);
      if (collection == null) {
        return false;
      } else {
        return collection.contains(key);
      }
    }
  }

  public static boolean containsAny(final Collection<?> collection1,
    final Collection<?> collection2) {
    for (final Object value : collection1) {
      if (collection2.contains(value)) {
        return true;
      }
    }
    return false;
  }

  public static <T> boolean containsReference(final List<WeakReference<T>> list, final T object) {
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

  public static <T> List<T> copy(final List<T> list) {
    if (list == null) {
      return new ArrayList<>();
    } else {
      return new ArrayList<>(list);
    }
  }

  public static <V> Set<V> createHashSet(final Collection<? extends V> set) {
    final Set<V> copy = new HashSet<V>();
    if (set != null) {
      copy.addAll(set);
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

  /**
   * Filter the collection by applying the filter.
   * @param collection
   * @param filter
   */
  public static <V, C extends Collection<V>> void filter(final Collection<V> collection,
    final Predicate<V> filter) {
    for (final Iterator<V> iterator = collection.iterator(); iterator.hasNext();) {
      final V record = iterator.next();
      if (!filter.test(record)) {
        iterator.remove();
      }
    }
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

  public static <K, V> int getCollectionSize(final Map<K, ? extends Collection<V>> map,
    final K key) {
    final Collection<V> values = map.get(key);
    if (values == null) {
      return 0;
    } else {
      return values.size();
    }
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

  public static <V> List<V> list(final Iterable<? extends V> values) {
    final ArrayList<V> list = new ArrayList<V>();
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
    return list;
  }

  public static <V> List<V> list(final V... values) {
    final ArrayList<V> list = new ArrayList<V>();
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
    return list;
  }

  public static <T> void removeReference(final List<WeakReference<T>> list, final T object) {
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
      final StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < string.length(); ++i) {
        char c = string.charAt(i);
        switch (c) {
          case '$':
            ++i;
            if (i < string.length()) {
              c = string.charAt(i);
              if (c == '{') {
                ++i;
                final StringBuilder propertyName = new StringBuilder();
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

  public static <K, V> boolean setContains(final Map<K, Set<V>> map, final K key, final V value) {
    if (map == null) {
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

  public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValues(
    final Map<K, V> map) {
    final MapValueComparator<K, V> comparator = new MapValueComparator<K, V>(map);
    final Map<K, V> sortedMap = new TreeMap<K, V>(comparator);
    sortedMap.putAll(map);
    return new LinkedHashMap<K, V>(sortedMap);
  }

  public static List<String> split(final String text, final String regex) {
    if (Property.hasValue(text)) {
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

  public static List<Double> toList(final double... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Double> list = new ArrayList<Double>();
      for (final double value : values) {
        list.add(value);
      }
      return list;
    }
  }

  public static List<Integer> toList(final int... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Integer> list = new ArrayList<Integer>();
      for (final int value : values) {
        list.add(value);
      }
      return list;
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> List<V> toList(final V... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(values);
    }
  }

  /**
   * Create a string using the same style as java.util.List.toString.
   * @param iterator
   * @return
   */
  public static String toListString(final Iterable<? extends Object> iterable) {
    if (iterable == null) {
      return "[]";
    } else {
      final Iterator<? extends Object> iterator = iterable.iterator();
      return toListString(iterator);
    }
  }

  public static String toListString(final Iterator<? extends Object> iterator) {
    if (iterator == null) {
      return "[]";
    } else {
      final StringBuilder string = new StringBuilder("[");
      if (iterator.hasNext()) {
        string.append(iterator.next());
        while (iterator.hasNext()) {
          string.append(", ");
          string.append(iterator.next());
        }
      }
      string.append("]");
      return string.toString();
    }
  }

  public static String toString(final boolean skipNulls, final String separator,
    final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      append(string, values, skipNulls, separator);
      return string.toString();
    }
  }

  public static String toString(final boolean skipNulls, final String separator,
    final Object... values) {
    return toString(skipNulls, separator, Arrays.asList(values));
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
  public static String toString(final String separator, final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      append(string, values, separator);
      return string.toString();
    }
  }

  public static String toString(final String separator, final int... values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      boolean first = true;
      for (final int value : values) {
        if (first) {
          first = false;
        } else {
          string.append(separator);
        }
        string.append(value);
      }
      return string.toString();
    }
  }

  public static String toString(final String separator, final Object... values) {
    return toString(separator, Arrays.asList(values));
  }

  public static List<String> toStringList(final Collection<?> values) {
    final List<String> strings = new ArrayList<>();
    if (values != null) {
      for (final Object value : values) {
        strings.add(StringConverterRegistry.toString(value));
      }
    }
    return strings;
  }

  private CollectionUtil() {
  }
}
