package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.revolsys.swing.Icons;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public interface Field extends Cloneable {
  Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  Icon ERROR_ICON = Icons.getIcon("exclamation");

  Field clone();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  default Component getComponent() {
    return (Component)this;
  }

  default String getFieldName() {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.getName();
  }

  default Color getFieldSelectedTextColor() {
    return DEFAULT_SELECTED_FOREGROUND;
  }

  FieldSupport getFieldSupport();

  default String getFieldValidationMessage() {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.getErrorMessage();
  }

  default <T> T getFieldValue() {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.getValue();
  }

  default UndoManager getUndoManager() {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.getUndoManager();
  }

  default boolean isFieldValid() {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.isFieldValid();
  }

  default boolean isHasValidValue() {
    updateFieldValue();
    final FieldSupport fieldSupport = getFieldSupport();
    final Object fieldValue = getFieldValue();
    return Property.hasValue(fieldValue) && fieldSupport.isFieldValid();
  }

  default void setEditable(final boolean editable) {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setEditable(editable);
  }

  default void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    if (this instanceof JComponent) {
      final JComponent component = (JComponent)this;
      component.setBackground(color);
    }
  }

  default void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    if (this instanceof JComponent) {
      final JComponent component = (JComponent)this;
      component.setForeground(color);
    }
  }

  default void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  default void setFieldSelectedTextColor(final Color color) {
  }

  default void setFieldToolTip(final String toolTip) {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setFieldToolTip(toolTip);
  }

  default void setFieldValid() {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setFieldValid();
  }

  default boolean setFieldValue(final Object value) {
    final FieldSupport fieldSupport = getFieldSupport();
    return fieldSupport.setValue(value);
  }

  default void setShowErrorIcon(final boolean showErrorIcon) {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setShowErrorIcon(showErrorIcon);
  }

  default void setUndoManager(final UndoManager undoManager) {
    final FieldSupport fieldSupport = getFieldSupport();
    fieldSupport.setUndoManager(undoManager);
  }

  void updateFieldValue();
}
