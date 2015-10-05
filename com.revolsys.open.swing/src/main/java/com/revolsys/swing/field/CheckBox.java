package com.revolsys.swing.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.swing.menu.PopupMenu;

public class CheckBox extends JCheckBox implements Field, ActionListener {

  private static final long serialVersionUID = 1L;

  private final FieldSupport fieldSupport;

  public CheckBox(final String fieldName) {
    this(fieldName, "");
  }

  public CheckBox(final String fieldName, final Object fieldValue) {
    this.fieldSupport = new FieldSupport(this, this, fieldName, fieldValue);
    setFieldValue(BooleanStringConverter.getBoolean(fieldValue));
    addActionListener(this);
    getUndoManager().addKeyMap(this);
    PopupMenu.getPopupMenuFactory(this);
    setOpaque(false);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    setFieldValue(isSelected());
  }

  @Override
  public Field clone() {
    return new CheckBox(getFieldName(), getFieldValue());
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)(Boolean)isSelected();
  }

  @Override
  public void setEditable(final boolean editable) {
    setEnabled(editable);
  }

  @Override
  public void setFieldValue(final Object value) {
    final boolean newValue = BooleanStringConverter.getBoolean(value);
    final boolean selected = isSelected();
    getUndoManager().discardAllEdits();
    if (newValue != selected) {
      setSelected(newValue);
      getUndoManager().discardAllEdits();
    }
    Field.super.setFieldValue(newValue);
  }

  @Override
  public void setToolTipText(final String text) {
    final FieldSupport fieldSupport = getFieldSupport();
    if (fieldSupport.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(isSelected());
  }
}
