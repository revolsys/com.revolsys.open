package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class Slider extends JSlider implements Field, FocusListener,
ChangeListener {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  private String errorMessage;

  private String fieldName;

  private int fieldValue;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public Slider(final String fieldName) {
    super();
    setFieldName(fieldName);
    addChangeListener(this);
  }

  public Slider(final String fieldName, final BoundedRangeModel brm) {
    super(brm);
    setFieldName(fieldName);
    addChangeListener(this);
  }

  public Slider(final String fieldName, final int orientation) {
    super(orientation);
    setFieldName(fieldName);
    addChangeListener(this);
  }

  public Slider(final String fieldName, final int min, final int max) {
    super(min, max);
    setFieldName(fieldName);
    addChangeListener(this);
  }

  public Slider(final String fieldName, final int min, final int max,
    final int value) {
    super(min, max, value);
    setFieldName(fieldName);
    addChangeListener(this);
    this.fieldValue = value;
  }

  public Slider(final String fieldName, final int orientation, final int min,
    final int max, final int value) {
    super(orientation, min, max, value);
    setFieldName(fieldName);
    addChangeListener(this);
    this.fieldValue = value;
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
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

  private void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
    if (Property.hasValue(fieldName)) {
      setToolTipText(CaseConverter.toCapitalizedWords(fieldName));
    }
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Number) {
      final Number newNumber = (Number)value;
      final int newValue = newNumber.intValue();
      final int oldValue = this.fieldValue;
      final int fieldValue = getValue();
      this.undoManager.discardAllEdits();
      if (fieldValue != newValue) {
        setValue(newValue);
        this.undoManager.discardAllEdits();
      }
      if (oldValue != newValue) {
        this.fieldValue = newValue;
        firePropertyChange(this.getFieldName(), oldValue, newValue);
        SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
          oldValue, newValue);
      }
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
