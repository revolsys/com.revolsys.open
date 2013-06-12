package com.revolsys.swing.field;

import java.beans.PropertyChangeListener;

public interface Field {
  void addPropertyChangeListener(String propertyName,
    PropertyChangeListener listener);

  String getFieldName();

  <T> T getFieldValue();

  void setEnabled(boolean enabled);

  void setFieldInvalid(String message);

  void setFieldValid();

  void setFieldValue(Object value);
}
