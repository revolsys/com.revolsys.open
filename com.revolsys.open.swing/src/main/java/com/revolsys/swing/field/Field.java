package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.ImageIcon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.undo.UndoManager;

public interface Field extends Cloneable {

  ImageIcon ERROR_ICON = Icons.getIcon("exclamation");

  Field clone();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  String getFieldName();

  String getFieldValidationMessage();

  <T> T getFieldValue();

  boolean isFieldValid();

  void setEditable(boolean editable);

  void setFieldBackgroundColor(Color color);

  void setFieldForegroundColor(Color color);

  void setFieldInvalid(String message, Color foregroundColor, Color backgroundColor);

  void setFieldToolTip(String toolTip);

  void setFieldValid();

  void setFieldValue(Object value);

  void setUndoManager(UndoManager undoManager);

  void updateFieldValue();
}
