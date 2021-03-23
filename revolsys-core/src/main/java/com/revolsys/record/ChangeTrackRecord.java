package com.revolsys.record;

public interface ChangeTrackRecord extends Record {

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

  /*
   * Create a shallow clone of this record. The ChangeTrackRecord shouldn't be
   * used after this.
   */
  Record newRecord();
}
