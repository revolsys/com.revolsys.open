package com.revolsys.record;

public interface ChangeTrackRecord extends Record {

  <T> T getOriginalValue(String name);

  boolean isModified(String fieldName);

  /*
   * Create a shallow clone of this record. The ChangeTrackRecord shouldn't be
   * used after this.
   */
  Record newRecord();
}
