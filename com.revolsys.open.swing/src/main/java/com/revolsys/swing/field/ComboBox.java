package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

@SuppressWarnings("serial")
public class ComboBox extends JComboBox implements Field {

  private String fieldName;

  private Object fieldValue;

  private String errorMessage;

  private String originalToolTip;

  public ComboBox() {
    this("fieldValue", null);
  }

  public ComboBox(final ComboBoxModel model) {
    this("fieldValue", model);
  }

  public ComboBox(final Object... items) {
    super(items);
  }

  public ComboBox(final String fieldName, final ComboBoxModel model) {
    super(model);
    this.fieldName = fieldName;
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Object selectedItem = getSelectedItem();
        setFieldValue(selectedItem);
      }
    });
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getSelectedItem();
  }

  @Override
  public void setFieldInvalid(final String message) {
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setForeground(Color.RED);
    component.setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  @Override
  public void setFieldValid() {
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setForeground(TextField.DEFAULT_FOREGROUND);
    component.setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(originalToolTip);
  }

  @Override
  public synchronized void setFieldValue(final Object value) {
    final Object oldValue = fieldValue;
    if (!EqualsRegistry.equal(getSelectedItem(), value)) {
      setSelectedItem(value);
    }
    this.fieldValue = value;
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
    return getFieldName();
  }
}
