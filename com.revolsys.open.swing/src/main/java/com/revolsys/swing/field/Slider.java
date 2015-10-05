package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Numbers;
import com.revolsys.util.Property;

public class Slider extends JSlider implements Field, FocusListener, ChangeListener {
  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  private static final long serialVersionUID = 1L;

  private final FieldSupport fieldSupport;

  public Slider(final String fieldName) {
    this(fieldName, HORIZONTAL, 0, 100, 50);
  }

  public Slider(final String fieldName, final int orientation) {
    this(fieldName, orientation, 0, 100, 50);
  }

  public Slider(final String fieldName, final int min, final int max) {
    this(fieldName, min, max, (min + max) / 2);
  }

  public Slider(final String fieldName, final int min, final int max, final int value) {
    this(fieldName, HORIZONTAL, min, max, value);
  }

  public Slider(final String fieldName, final int orientation, final int min, final int max,
    final int value) {
    super(orientation, min, max, value);
    if (Property.hasValue(fieldName)) {
      setToolTipText(CaseConverter.toCapitalizedWords(fieldName));
    }
    this.fieldSupport = new FieldSupport(this, fieldName, value);
    addChangeListener(this);
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
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
    final int value = getValue();
    setFieldValue(value);
  }

  protected String getDisplayText(final Object value) {
    return StringConverterRegistry.toString(value);
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    final Integer value = getValue();
    return (T)value;
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setEditable(final boolean editable) {
    setEnabled(editable);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final Integer newValue = Numbers.toInteger(value);
    final Integer fieldValue = getValue();
    getUndoManager().discardAllEdits();
    if (Numbers.equal(fieldValue, newValue)) {
      setValue(newValue);
      getUndoManager().discardAllEdits();
    }
    this.fieldSupport.setValue(newValue);

  }

  @Override
  public void setToolTipText(final String text) {
    if (this.fieldSupport.setOriginalTooltipText(text)) {
      setFieldToolTip(text);
    }
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    updateFieldValue();
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getValue());
  }
}
