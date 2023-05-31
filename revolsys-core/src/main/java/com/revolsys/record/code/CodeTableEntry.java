package com.revolsys.record.code;

import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;

public interface CodeTableEntry {
  static CodeTableEntry create(Identifier identifier, Object value) {
    return new SingleValueCodeTableEntry(identifier, value);
  }

  static Identifier getIdentifier(final CodeTableEntry entry) {
    if (entry == null) {
      return null;
    } else {
      return entry.getIdentifier();
    }
  }

  @SuppressWarnings("unchecked")
  static <V> V getValue(CodeTableEntry entry) {
    if (entry == null) {
      return null;
    } else {
      return (V)entry.getValue();
    }
  }

  static List<Object> getValues(CodeTableEntry entry) {
    if (entry == null) {
      return null;
    } else {
      return entry.getValues();
    }
  }

  static int maxLength(Iterable<CodeTableEntry> entries) {
    int length = 0;
    for (final CodeTableEntry entry : entries) {
      final int valueLength = entry.getLength();
      if (valueLength > length) {
        length = valueLength;
      }
    }
    return length;
  }

  default boolean equalsValue(Object value) {
    return DataType.equal(value, getValue());
  }

  Identifier getIdentifier();

  default int getLength() {
    return getValue().toString().length();
  }

  <V> V getValue();

  List<Object> getValues();
}
