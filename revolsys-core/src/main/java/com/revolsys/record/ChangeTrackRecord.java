package com.revolsys.record;

public interface ChangeTrackRecord extends Record {

  <T> T getOriginalValue(String name);

  boolean isModified(String fieldName);

}
