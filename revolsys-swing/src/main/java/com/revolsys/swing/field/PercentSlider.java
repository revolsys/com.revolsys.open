package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Floats;
import org.jeometry.common.number.Numbers;

import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class PercentSlider extends JSlider implements Field, FocusListener, ChangeListener {
  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  private static final long serialVersionUID = 1L;

  private static final float SCALE_FACTOR = 1000;

  private final FieldSupport fieldSupport;

  public PercentSlider(final String fieldName) {
    this(fieldName, HORIZONTAL);
  }

  public PercentSlider(final String fieldName, final float value) {
    this(fieldName, HORIZONTAL, value);
  }

  public PercentSlider(final String fieldName, final int orientation) {
    this(fieldName, orientation, (float)0.5);
  }

  public PercentSlider(final String fieldName, final int orientation, final float value) {
    super(orientation, 0, 1000, Math.round(value * SCALE_FACTOR));
    setMajorTickSpacing(500);
    setMinorTickSpacing(100);
    setPaintTicks(true);
    setPaintLabels(true);
    final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(1000, new JLabel("100"));
    setLabelTable(labelTable);

    if (Property.hasValue(fieldName)) {
      setToolTipText(CaseConverter.toCapitalizedWords(fieldName));
    }
    this.fieldSupport = new FieldSupport(this, fieldName, value, true);
    addChangeListener(this);
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
    final Float value = getFieldValue();
    setFieldValue(value);
  }

  protected String getDisplayText(final Object value) {
    return DataTypes.toString(value);
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    final int value = getValue();
    return (T)(Float)(value / SCALE_FACTOR);
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
  public boolean setFieldValue(final Object value) {
    final Float newValue = Floats.toFloat(value);
    final Float fieldValue = getFieldValue();
    getUndoManager().discardAllEdits();
    if (Numbers.equal(fieldValue, newValue)) {
      setValue(Math.round(newValue * SCALE_FACTOR));
      getUndoManager().discardAllEdits();
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
  public void stateChanged(final ChangeEvent e) {
    updateFieldValue();
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    final float value = getFieldValue();
    setFieldValue(value);
  }
}
