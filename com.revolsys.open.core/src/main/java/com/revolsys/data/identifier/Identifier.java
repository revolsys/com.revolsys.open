package com.revolsys.data.identifier;

import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Record;

public interface Identifier extends Comparable<Identifier> {
  Integer getInteger(int index);

  Long getLong(int index);

  String getString(int index);

  <V> V getValue(int index);

  List<Object> getValues();

  void setIdentifier(Record record);

  void setIdentifier(Map<String, Object> record, List<String> attributeNames);

  void setIdentifier(Map<String, Object> record, String... attributeNames);
}
