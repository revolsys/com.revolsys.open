package com.revolsys.swing.field;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JFormattedTextField;

import org.jdesktop.swingx.JXDatePicker;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class DateField extends JXDatePicker implements Field,
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private String fieldName;

  private String errorMessage;

  private Date fieldValue;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public DateField() {
    this("fieldValue");
  }

  public DateField(final String fieldName) {
    this.fieldName = fieldName;
    Property.addListener(this, "date", this);
    this.undoManager.addKeyMap(getEditor());
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
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
    return (T)this.fieldValue;
  }

  @Override
  public boolean isFieldValid() {
    return true;
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
      color = TextField.DEFAULT_BACKGROUND;
    }
    final JFormattedTextField editor = getEditor();
    editor.setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    final JFormattedTextField editor = getEditor();
    editor.setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    final JFormattedTextField editor = getEditor();
    editor.setForeground(foregroundColor);
    editor.setSelectedTextColor(foregroundColor);
    editor.setBackground(backgroundColor);
    this.errorMessage = message;
    setFieldToolTip(this.errorMessage);
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    final JFormattedTextField editor = getEditor();
    editor.setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    final JFormattedTextField editor = getEditor();
    editor.setForeground(TextField.DEFAULT_FOREGROUND);
    editor.setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    editor.setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    setFieldToolTip(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final Date oldValue = this.fieldValue;
    final Date date = StringConverterRegistry.toObject(Date.class, value);
    if (!EqualsRegistry.equal(getDate(), date)) {
      setDate(date);
    }
    if (!EqualsRegistry.equal(oldValue, date)) {
      this.fieldValue = date;
      firePropertyChange(this.fieldName, oldValue, date);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
        oldValue, date);
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
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    // TODO setFieldValue(getText());
  }
}
