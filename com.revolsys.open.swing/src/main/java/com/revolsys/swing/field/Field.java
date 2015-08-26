package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.revolsys.swing.Icons;
import com.revolsys.swing.undo.UndoManager;

public interface Field extends Cloneable {

  Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  ImageIcon ERROR_ICON = Icons.getIcon("exclamation");

  Field clone();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  String getFieldName();

  String getFieldValidationMessage();

  <T> T getFieldValue();

  boolean isFieldValid();

  void setEditable(boolean editable);

  default void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_BACKGROUND;
    }
    if (this instanceof JComponent) {
      final JComponent component = (JComponent)this;
      component.setBackground(color);
    }
  }

  default void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_BACKGROUND;
    }
    if (this instanceof JComponent) {
      final JComponent component = (JComponent)this;
      component.setForeground(color);
    }
  }

  void setFieldInvalid(String message, Color foregroundColor, Color backgroundColor);

  void setFieldToolTip(String toolTip);

  void setFieldValid();

  void setFieldValue(Object value);

  void setUndoManager(UndoManager undoManager);

  void updateFieldValue();
}
