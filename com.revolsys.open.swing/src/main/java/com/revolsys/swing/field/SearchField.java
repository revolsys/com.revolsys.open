package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.jdesktop.swingx.JXSearchField;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.util.OS;

public class SearchField extends JXSearchField implements FocusListener, Field {
  private static final long serialVersionUID = 1L;

  private final FieldSupport fieldSupport;

  public SearchField() {
    this("fieldValue");
  }

  public SearchField(final String fieldName) {
    this.fieldSupport = new FieldSupport(this, fieldName, "", false);
    setOpaque(true);
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
    super.setToolTipText(toolTip);
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
  public void setUseNativeSearchFieldIfPossible(boolean useNativeSearchFieldIfPossible) {
    if (OS.isMac()) {
      useNativeSearchFieldIfPossible = false;
    }
    super.setUseNativeSearchFieldIfPossible(useNativeSearchFieldIfPossible);
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
