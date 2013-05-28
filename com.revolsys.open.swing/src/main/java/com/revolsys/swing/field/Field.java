package com.revolsys.swing.field;

public interface Field<V> {
  void setFieldValue(V value);

  <T> T getFieldValue();
  
  String getFieldName();
}
