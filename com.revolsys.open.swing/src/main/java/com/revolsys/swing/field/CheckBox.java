package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class CheckBox extends JCheckBox implements Field, ActionListener {

  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private boolean fieldValue;

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public CheckBox(final String fieldName) {
    this(fieldName, "");
  }

  public CheckBox(final String fieldName, final Object fieldValue) {
    if (Property.hasValue(fieldName)) {
      this.fieldName = fieldName;
    } else {
      this.fieldName = "fieldValue";
    }
    setFieldValue(BooleanStringConverter.getBoolean(fieldValue));
    addActionListener(this);
    this.undoManager.addKeyMap(this);
    PopupMenu.getPopupMenuFactory(this);
    setOpaque(false);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    setFieldValue(isSelected());
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)(Boolean)isSelected();
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_FOREGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    setForeground(foregroundColor);
    setBackground(backgroundColor);
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(CheckBox.DEFAULT_FOREGROUND);
    setBackground(CheckBox.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final boolean newValue = BooleanStringConverter.getBoolean(value);
    final boolean oldValue = this.fieldValue;
    final boolean selected = isSelected();
    this.undoManager.discardAllEdits();
    if (newValue != selected) {
      setSelected(newValue);
      this.undoManager.discardAllEdits();
    }
    if (oldValue != newValue) {
      this.fieldValue = newValue;
      firePropertyChange(this.fieldName, oldValue, newValue);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
        oldValue, newValue);
    }
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!Property.hasValue(this.errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(isSelected());
  }
}
