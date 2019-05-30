package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.MenuFactory;

public class TextArea extends JTextArea implements Field, FocusListener {
  private static final long serialVersionUID = 1L;

  private final FieldSupport fieldSupport;

  public TextArea() {
    this("fieldValue");
  }

  public TextArea(final int rows, final int columns) {
    this("text", rows, columns);
  }

  public TextArea(final String fieldName) {
    this(fieldName, "");
  }

  public TextArea(final String fieldName, final int rows, final int columns) {
    this(fieldName, "", rows, columns);
  }

  public TextArea(final String fieldName, final Object fieldValue) {
    this(fieldName, fieldValue, 0, 0);
  }

  public TextArea(final String fieldName, final Object fieldValue, final int rows,
    final int columns) {
    final String text = DataTypes.toString(fieldValue);
    this.fieldSupport = new FieldSupport(this, fieldName, text, true);
    setRows(rows);
    setColumns(columns);
    setDocument(new PropertyChangeDocument(this));
    setText(text);
    addFocusListener(new WeakFocusListener(this));
    MenuFactory.getPopupMenuFactory(this);
    setRows(rows);
    setColumns(columns);
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
  }

  @Override
  public Color getFieldSelectedTextColor() {
    return getSelectedTextColor();
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getText();
  }

  @Override
  public void setFieldSelectedTextColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_SELECTED_FOREGROUND;
    }
    setSelectedTextColor(color);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public boolean setFieldValue(final Object value) {
    final String newValue = DataTypes.toString(value);
    if (!DataType.equal(getText(), newValue)) {
      setText(newValue);
    }
    return this.fieldSupport.setValue(newValue);
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.fieldSupport == null || this.fieldSupport.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getText());
  }
}
