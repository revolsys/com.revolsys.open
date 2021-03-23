package com.revolsys.record;

public interface ChangeTrackRecord extends Record {

  static ChangeTrackRecord of(final Record record) {
    return new ChangeTrackRecordImpl(record);
  }

  <T> T getOriginalValue(int fieldIndex);

  default <T> T getOriginalValue(final String fieldName) {
    final int fieldIndex = getFieldIndex(fieldName);
    return getOriginalValue(fieldIndex);
  }

  boolean isModified(int fieldIndex);

  default boolean isModified(final String fieldName) {
    final int fieldIndex = getFieldIndex(fieldName);
    return isModified(fieldIndex);
  }
}
