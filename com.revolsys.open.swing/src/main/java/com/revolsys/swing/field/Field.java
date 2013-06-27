package com.revolsys.swing.field;

import java.awt.Color;
import java.beans.PropertyChangeListener;

public interface Field {
  void addPropertyChangeListener(String propertyName,
    PropertyChangeListener listener);

  String getFieldName();

  <T> T getFieldValue();

  void setEnabled(boolean enabled);

  void setFieldBackgroundColor(Color color);

  void setFieldForegroundColor(Color color);

  void setFieldInvalid(String message);

  void setFieldToolTip(String toolTip);

  void setFieldValid();

  void setFieldValue(Object value);
}
