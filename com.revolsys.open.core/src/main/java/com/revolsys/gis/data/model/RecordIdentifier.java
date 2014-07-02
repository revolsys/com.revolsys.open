package com.revolsys.gis.data.model;

import java.util.List;
import java.util.Map;

public interface RecordIdentifier extends Comparable<RecordIdentifier> {
  Integer getInteger(int index);

  Long getLong(int index);

  String getString(int index);

  <V> V getValue(int index);

  List<Object> getValues();

  void setIdentifier(DataObject record);

  void setIdentifier(Map<String, Object> record, List<String> attributeNames);

  void setIdentifier(Map<String, Object> record, String... attributeNames);
}
