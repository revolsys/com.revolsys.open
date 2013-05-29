package com.revolsys.swing.field;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.jdesktop.swingx.JXTextField;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class TextField extends JXTextField implements Field<String>,
  FocusListener {

  private String fieldName;

  private String fieldValue;

  public TextField() {
    this(null);
  }

  public TextField(String fieldName) {
    this(fieldName, "");
  }

  public TextField(String fieldName, Object fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setText(this.fieldValue);
    addFocusListener(this);
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
  public void setFieldValue(String value) {
    String oldValue = fieldValue;
    if (!EqualsRegistry.equal(getText(), value)) {
      setText(value);
    }
    firePropertyChange(fieldName, oldValue, value);
  }

  @Override
  public void focusGained(FocusEvent e) {
  }

  @Override
  public void focusLost(FocusEvent e) {
    String text = getText();
    setFieldValue(text);
  }
}
