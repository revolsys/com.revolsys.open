package com.revolsys.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

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

  public static boolean containsAny(final Collection<?> collection1,
    final Collection<?> collection2) {
    for (final Object value : collection1) {
      if (collection2.contains(value)) {
        return true;
      }
    }
    return false;
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

  private CollectionUtil() {
  }
}
