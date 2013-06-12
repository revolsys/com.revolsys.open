package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.jdesktop.swingx.JXTextArea;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.menu.PopupMenu;

@SuppressWarnings("serial")
public class TextArea extends JXTextArea implements Field, FocusListener {

  private final String fieldName;

  private final String fieldValue;

  private String errorMessage;

  public TextArea() {
    this("fieldValue");
  }

  public TextArea(final int rows, final int columns) {
    this(null);
    setRows(rows);
    setColumns(columns);
  }

  public TextArea(final String fieldName) {
    this(fieldName, "");
  }

  public TextArea(final String fieldName, final int rows, final int columns) {
    this(fieldName, "");
    setRows(rows);
    setColumns(columns);
  }

  public TextArea(final String fieldName, final Object fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setText(this.fieldValue);
    addFocusListener(this);
    PopupMenu.getPopupMenuFactory(this);
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
  public String getFieldName() {
    return fieldName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getText();
  }

  @Override
  public String getToolTipText() {
    if (StringUtils.hasText(errorMessage)) {
      return errorMessage;
    } else {
      return super.getToolTipText();
    }
  }

  @Override
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setSelectedTextColor(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
  }

  @Override
  public void setFieldValue(final Object value) {
    final String newValue = StringConverterRegistry.toString(value);
    final String oldValue = fieldValue;
    if (!EqualsRegistry.equal(getText(), newValue)) {
      setText(newValue);
    }
    firePropertyChange(fieldName, oldValue, newValue);
  }
}
