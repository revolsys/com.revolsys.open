package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.jdesktop.swingx.JXSearchField;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class SearchField extends JXSearchField implements FocusListener, Field {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private String errorMessage;

  private String fieldValue;

  private String originalToolTip;

  public SearchField() {
    this("fieldValue");
  }

  public SearchField(final String fieldName) {
    this.fieldName = fieldName;
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
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setSelectedTextColor(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(originalToolTip);
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

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

}
