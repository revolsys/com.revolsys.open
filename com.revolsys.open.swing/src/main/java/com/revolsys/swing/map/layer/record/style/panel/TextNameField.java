package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.component.AttributeTitleStringConveter;
import com.revolsys.util.Property;

public class TextNameField extends ValueField {

  private final TextArea textNameField;

  private final ComboBox fieldNamesField;

  public TextNameField(final AbstractRecordLayer layer,
    final String fieldName, final Object fieldValue) {
    super(new BorderLayout());
    textNameField = new TextArea(fieldName, fieldValue, 3, 30);
    add(new JScrollPane(textNameField), BorderLayout.NORTH);

    final ArrayList<String> fieldNames = new ArrayList<String>(
      layer.getFieldNames());
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    fieldNames.remove(recordDefinition.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      layer);
    this.fieldNamesField = new ComboBox(converter, false, fieldNames.toArray());
    this.fieldNamesField.setRenderer(converter);

    final JButton addButton = InvokeMethodAction.createButton(null,
      "Add field name", Icons.getIcon("add"), this, "addFieldName");
    addButton.setIcon(Icons.getIcon("add"));
    addButton.setToolTipText("Add field Name");

    final BasePanel fieldNamesPanel = new BasePanel(new FlowLayout(
      FlowLayout.LEFT), this.fieldNamesField, addButton);
    GroupLayoutUtil.makeColumns(fieldNamesPanel, 2, false);
    add(fieldNamesPanel, BorderLayout.SOUTH);

  }

  public void addFieldName() {
    final String selectedFieldName = (String)fieldNamesField.getSelectedItem();
    if (Property.hasValue(selectedFieldName)) {
      final int position = textNameField.getCaretPosition();
      final Document document = textNameField.getDocument();
      try {
        document.insertString(position, "[" + selectedFieldName + "]", null);
      } catch (final BadLocationException e) {
      }
      ((JComponent)textNameField).requestFocusInWindow();
    }
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    super.addPropertyChangeListener(propertyName, listener);
    Property.addListener(textNameField, propertyName, listener);
  }

  @Override
  public String getFieldValidationMessage() {
    return textNameField.getFieldValidationMessage();
  }

  @Override
  public <T> T getFieldValue() {
    return textNameField.getFieldValue();
  }

  @Override
  public boolean isFieldValid() {
    return textNameField.isFieldValid();
  }

  @Override
  public void setFieldBackgroundColor(final Color color) {
    textNameField.setFieldBackgroundColor(color);
  }

  @Override
  public void setFieldForegroundColor(final Color color) {
    textNameField.setFieldForegroundColor(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    textNameField.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    textNameField.setFieldToolTip(toolTip);
  }

  @Override
  public void setFieldValid() {
    textNameField.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    textNameField.setFieldValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    textNameField.setToolTipText(text);
  }

  @Override
  public void updateFieldValue() {
    textNameField.updateFieldValue();
  }
}
