package com.revolsys.swing.field;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JFormattedTextField;

import org.jdesktop.swingx.JXDatePicker;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.util.Property;

public class DateField extends JXDatePicker implements Field, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final FieldSupport fieldSupport;

  public DateField() {
    this("fieldValue");
  }

  public DateField(final String fieldName) {
    this(fieldName, null);
  }

  public DateField(final String fieldName, final Object fieldValue) {
    this.fieldSupport = new FieldSupport(this, fieldName, fieldValue, true);
    Property.addListener(this, "date", this);
    getUndoManager().addKeyMap(getEditor());
  }

  @Override
  public DateField clone() {
    return new DateField(getFieldName(), getFieldValue());
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public Color getFieldSelectedTextColor() {
    return getEditor().getSelectedTextColor();
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
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
    super.setDate(date);
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_BACKGROUND;
    }
    final JFormattedTextField editor = getEditor();
    editor.setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_BACKGROUND;
    }
    final JFormattedTextField editor = getEditor();
    editor.setForeground(color);
  }

  @Override
  public void setFieldSelectedTextColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_SELECTED_FOREGROUND;
    }
    final JFormattedTextField editor = getEditor();
    editor.setSelectedTextColor(color);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    final JFormattedTextField editor = getEditor();
    editor.setToolTipText(toolTip);
  }

  @Override
  public boolean setFieldValue(final Object value) {
    final Date date = DataTypes.DATE_TIME.toObject(value);
    if (!DataType.equal(getDate(), date)) {
      setDate(date);
    }
    return this.fieldSupport.setValue(date);
  }

  @Override
  public void setToolTipText(final String text) {
    final FieldSupport fieldSupport = getFieldSupport();
    if (fieldSupport == null || fieldSupport.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    // TODO setFieldValue(getText());
  }
}
