package com.revolsys.collection.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.util.Property;

public interface Lists {
  public static <V> void addAll(final List<V> list, final Iterable<? extends V> values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

  public static <V> void addAll(final List<V> list, final Stream<? extends V> values) {
    if (values != null) {
      values.forEach(list::add);
    }
  }

  public static <V> void addAll(final List<V> list,
    @SuppressWarnings("unchecked") final V... values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

  /**
  * Add the value to the list if it is not empty and not already in the list.
  * @param list
  * @param value
  * @return
  */
  public static <V> boolean addNotContains(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        list.add(index, value);
        return true;
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContains(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        return list.add(value);
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContainsLast(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (list.isEmpty() || !list.get(list.size() - 1).equals(value)) {
        list.add(value);
        return true;
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotEmpty(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      list.add(index, value);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotEmpty(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      return list.add(value);
    } else {
      return false;
    }
  }

  public static <V> Supplier<List<V>> arrayFactory() {
    return () -> {
      return new ArrayList<V>();
    };
  }

  static List<? extends Object> arrayToList(final Object value) {
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

  static <T> boolean containsReference(final List<WeakReference<T>> list, final T object) {
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

  static boolean equalsNotNull(final List<?> list1, final List<?> list2) {
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value1 = list1.get(i);
        final Object value2 = list2.get(i);
        if (!DataType.equal(value1, value2)) {
          return false;
        }
      }
    }
    return true;
  }

  static boolean equalsNotNull(final List<?> list1, final List<?> list2,
    final Collection<? extends CharSequence> exclude) {
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value1 = list1.get(i);
        final Object value2 = list2.get(i);
        if (!DataType.equal(value1, value2, exclude)) {
          return false;
        }
      }
    }
    return true;
  }

  static int getClassCount(final List<?> list, final Class<?> clazz) {
    int count = 0;
    for (int i = 0; i < list.size(); i++) {
      final Object value = list.get(i);
      if (value == null) {
        list.remove(i);
      } else if (clazz.isAssignableFrom(value.getClass())) {
        count++;
      }
    }
    return count++;
  }

  static <T> int getReferenceClassCount(final List<WeakReference<T>> list, final Class<?> clazz) {
    int count = 0;
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<?> reference = list.get(i);
      final Object value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (clazz.isAssignableFrom(value.getClass())) {
        count++;
      }
    }
    return count++;
  }

  static <T> List<T> getReferences(final List<WeakReference<T>> list) {
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

  public static <V> LinkedList<V> linked(@SuppressWarnings("unchecked") final V... values) {
    final LinkedList<V> list = new LinkedList<>();
    addAll(list, values);
    return list;
  }

  public static <V> Supplier<List<V>> linkedFactory() {
    return () -> {
      return new LinkedList<V>();
    };
  }

  static List<Double> newArray(final double... values) {
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

  static List<Integer> newArray(final int... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Integer> list = new ArrayList<>();
      for (final int value : values) {
        list.add(value);
      }
      return list;
    }
  }

  public static <V> ArrayList<V> newArray(@SuppressWarnings("unchecked") final V... values) {
    final ArrayList<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  static <T> void removeReference(final List<WeakReference<T>> list, final T object) {
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

  static List<String> split(final String text, final String regex) {
    if (Property.hasValue(text)) {
      return Arrays.asList(text.split(regex));
    } else {
      return Collections.emptyList();
    }
  }

  public static <V> List<V> toArray(final Iterable<? extends V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  static <T> List<T> toArray(final Iterable<T> iterable, final int size) {
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

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <V> List<V> toArray(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof List) {
      return (List)value;
    } else if (value instanceof Iterable) {
      final Iterable<Object> iterable = (Iterable)value;
      return (List<V>)toArray(iterable);
    } else if (value instanceof Number) {
      final List<V> list = new ArrayList<>();
      list.add((V)value);
      return list;
    } else {
      final String string = DataTypes.toString(value);
      return toArray(string);
    }
  }

  public static <V> List<V> toArray(final Stream<? extends V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  @SuppressWarnings("unchecked")
  static <V> List<V> toArray(final String string) {
    final Object value = JsonParser.read(string);
    if (value instanceof List) {
      return (List<V>)value;
    } else {
      throw new IllegalArgumentException("Value must be a JSON list " + string);
    }
  }

  static String toString(final Object value) {
    final Collection<?> collection;
    if (value instanceof Collection) {
      collection = (Collection<?>)value;
    } else {
      collection = toArray(value);
    }
    if (value == null) {
      return null;
    } else {

      final StringBuilder string = new StringBuilder("[");
      for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext();) {
        final Object object = iterator.next();
        final String stringValue = DataTypes.toString(object);
        string.append(stringValue);
        if (iterator.hasNext()) {
          string.append(", ");
        }
      }
      string.append("]");
      return string.toString();
    }

  }

  public static <V> List<V> unmodifiable(final Iterable<? extends V> values) {
    return new UnmodifiableArrayList<V>(values);
  }

  public static <V> List<V> unmodifiable(@SuppressWarnings("unchecked") final V... values) {
    return new UnmodifiableArrayList<V>(values);
  }
}
