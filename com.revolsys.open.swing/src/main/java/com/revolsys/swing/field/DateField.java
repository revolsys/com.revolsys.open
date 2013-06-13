package com.revolsys.swing.field;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JFormattedTextField;

import org.jdesktop.swingx.JXDatePicker;
import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class DateField extends JXDatePicker implements Field,
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private String fieldName;

  private String errorMessage;

  private Date fieldValue;

  private String originalToolTip;

  public DateField() {
    this("fieldValue");
  }

  public DateField(final String fieldName) {
    this.fieldName = fieldName;
    addPropertyChangeListener("date", this);
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)fieldValue;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final String propertyName = evt.getPropertyName();
    if (propertyName.equals("date")) {
      final Object date = evt.getNewValue();
      setFieldValue(date);
    }

  }

  @Override
  public void setDate(final Date date) {

    // System.out.println(date);
    super.setDate(date);
    // setFieldValue(date);
  }

  @Override
  public void setFieldInvalid(final String message) {
    final JFormattedTextField editor = getEditor();
    editor.setForeground(Color.RED);
    editor.setSelectedTextColor(Color.RED);
    editor.setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setFieldValid() {
    final JFormattedTextField editor = getEditor();
    editor.setForeground(TextField.DEFAULT_FOREGROUND);
    editor.setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    editor.setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final Date oldValue = fieldValue;
    final Date date = (Date)value;
    if (!EqualsRegistry.equal(getDate(), value)) {
      setDate(date);
    }
    this.fieldValue = date;
    firePropertyChange(fieldName, oldValue, value);
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
