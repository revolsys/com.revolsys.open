package com.revolsys.identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.revolsys.collection.map.Maps;
import com.revolsys.collection.set.Sets;
import com.revolsys.datatype.DataTypes;
import com.revolsys.record.Record;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.number.Numbers;

public interface Identifier {
  Identifier NULL = new SingleIdentifier(null);

  static Comparator<Identifier> comparator() {
    return (identifier1, identifier2) -> {
      if (identifier1 == identifier2) {
        return 0;
      } else if (identifier1 == null) {
        return 1;
      } else {
        return identifier1.compareTo(identifier2);
      }
    };
  }

  /**
   * Check that the two identifiers are equal. If either are null then false will be returned.
   *
   * @param identifier1
   * @param identifier2
   * @return True if the identifiers are not null and are equal. False otherwise.
   */
  static boolean equals(final Identifier identifier1, final Identifier identifier2) {
    if (identifier1 == null) {
      return false;
    } else if (identifier2 == null) {
      return false;
    } else {
      return identifier1.equals(identifier2);
    }
  }

  static Identifier newIdentifier(final Object... values) {
    if (values == null || values.length == 0) {
      return null;
    } else if (values.length == 1) {
      final Object value = values[0];
      return newIdentifier(value);
    } else {
      return new ListIdentifier(values);
    }
  }

  static Identifier newIdentifier(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      final long longValue = (Long)value;
      if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
        return new LongIdentifier(longValue);
      } else {
        final int intValue = (int)longValue;
        return new IntegerIdentifier(intValue);
      }
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      final int intValue = number.intValue();
      return new IntegerIdentifier(intValue);
    } else if (value instanceof Identifier) {
      return (Identifier)value;
    } else if (value instanceof Collection) {
      final Collection<?> idValues = (Collection<?>)value;
      return new ListIdentifier(idValues);
    } else {
      return new SingleIdentifier(value);
    }
  }

  static <V> TreeMap<Identifier, V> newTreeMap() {
    return new TreeMap<>(comparator());
  }

  static <V> Map<Identifier, V> newTreeMap(final Map<Identifier, ? extends V> map) {
    return Maps.newTree(comparator(), map);
  }

  static TreeSet<Identifier> newTreeSet() {
    return new TreeSet<>(comparator());
  }

  static TreeSet<Identifier> newTreeSet(final Iterable<Identifier> values) {
    return Sets.newTree(comparator(), values);
  }

  static void setIdentifier(final Map<String, Object> record, final List<String> idFieldNames,
    final Identifier identifier) {
    if (identifier == null) {
      for (int i = 0; i < idFieldNames.size(); i++) {
        final String fieldName = idFieldNames.get(0);
        record.put(fieldName, null);
      }
    } else {
      identifier.setIdentifier(record, idFieldNames);
    }
  }

  default int compareTo(final Identifier identifier2) {
    if (identifier2 == this) {
      return 0;
    } else if (identifier2 == null) {
      return -1;
    } else {
      final Iterator<Object> valueIter1 = getValues().iterator();
      final Iterator<Object> valueIter2 = identifier2.getValues().iterator();
      while (valueIter1.hasNext() && valueIter2.hasNext()) {
        final Object value1 = valueIter1.next();
        final Object value2 = valueIter2.next();
        final int compare = CompareUtil.compare(value1, value2);
        if (compare != 0) {
          return compare;
        }
      }
      if (valueIter1.hasNext()) {
        return 1;
      } else if (valueIter2.hasNext()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  default Integer getInteger(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  default Long getLong(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      return Long.valueOf(value.toString());
    }
  }

  default String getString(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else {
      return DataTypes.toString(value);
    }
  }

  @SuppressWarnings("unchecked")
  default <V> V getValue(final int index) {
    return (V)getValues().get(index);
  }

  List<Object> getValues();

  default boolean isSingle() {
    return getValues().size() == 1;
  }

  default void setIdentifier(final Map<String, Object> record, final List<String> fieldNames) {
    final List<Object> values = getValues();
    if (fieldNames.size() == values.size()) {
      for (int i = 0; i < fieldNames.size(); i++) {
        final String fieldName = fieldNames.get(i);
        final Object value = values.get(i);
        record.put(fieldName, value);
      }
    } else {
      throw new IllegalArgumentException(
        "Field names count for " + fieldNames + " != count for values " + values);
    }
  }

  default void setIdentifier(final Map<String, Object> record, final String... fieldNames) {
    setIdentifier(record, Arrays.asList(fieldNames));
  }

  default void setIdentifier(final Record record) {
    setIdentifier(record, record.getRecordDefinition().getFieldNames());
  }

  @SuppressWarnings("unchecked")
  default <V> V toSingleValue() {
    final List<Object> values = getValues();
    if (values.size() == 0) {
      return null;
    } else if (values.size() == 1) {
      return (V)values.get(0);
    } else {
      throw new IllegalArgumentException(
        "Cannot create value for identifier with multiple parts " + this);
    }
  }
}
