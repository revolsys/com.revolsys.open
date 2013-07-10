package com.revolsys.swing.field;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import com.revolsys.swing.undo.UndoManager;

public interface Field {
  void addPropertyChangeListener(String propertyName,
    PropertyChangeListener listener);

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  String getFieldName();

  String getFieldValidationMessage();

  <T> T getFieldValue();

  boolean isFieldValid();

  void setEnabled(boolean enabled);

  void setFieldBackgroundColor(Color color);

  void setFieldForegroundColor(Color color);

  void setFieldInvalid(String message);

  void setFieldToolTip(String toolTip);

  void setFieldValid();

  void setFieldValue(Object value);

  void setUndoManager(UndoManager undoManager);

}
