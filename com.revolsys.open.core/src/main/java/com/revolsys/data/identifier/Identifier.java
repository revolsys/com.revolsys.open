package com.revolsys.data.identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.util.Numbers;

public interface Identifier extends Comparable<Identifier> {
  static Identifier create(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      final long longValue = (Long)value;
      final int intValue = (int)longValue;
      if (longValue == intValue) {
        return new IntegerIdentifier(intValue);
      } else {
        return new LongIdentifier(longValue);
      }
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      return new IntegerIdentifier(number.intValue());
    } else if (value instanceof Identifier) {
      return (Identifier)value;
    } else if (value instanceof Collection) {
      final Collection<?> idValues = (Collection<?>)value;
      return new ListIdentifier(idValues);
    } else {
      return new SingleIdentifier(value);
    }
  }

  Integer getInteger(int index);

  Long getLong(int index);

  String getString(int index);

  <V> V getValue(int index);

  List<Object> getValues();

  void setIdentifier(Map<String, Object> record, List<String> fieldNames);

  void setIdentifier(Map<String, Object> record, String... fieldNames);

  void setIdentifier(Record record);
}
