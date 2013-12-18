package com.revolsys.swing.map.layer.dataobject.style.panel;

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

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.component.AttributeTitleStringConveter;

public class QueryFilterField extends ValueField {

  private final TextArea filterField;

  private final ComboBox fieldNamesField;

  public QueryFilterField(final AbstractDataObjectLayer layer,
    final String fieldName, final Object fieldValue) {
    super(new BorderLayout());
    filterField = new TextArea(fieldName, fieldValue, 5, 30);
    add(new JScrollPane(filterField), BorderLayout.NORTH);

    final ArrayList<String> fieldNames = new ArrayList<String>(
      layer.getColumnNames());
    final DataObjectMetaData metaData = layer.getMetaData();
    fieldNames.remove(metaData.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      layer);
    this.fieldNamesField = new ComboBox(converter, false, fieldNames.toArray());
    this.fieldNamesField.setRenderer(converter);

    final JButton addButton = InvokeMethodAction.createButton(null,
      "Add field name", SilkIconLoader.getIcon("add"), this, "addFieldName");
    addButton.setIcon(SilkIconLoader.getIcon("add"));
    addButton.setToolTipText("Add field Name");

    final BasePanel fieldNamesPanel = new BasePanel(new FlowLayout(
      FlowLayout.LEFT), this.fieldNamesField, addButton);
    GroupLayoutUtil.makeColumns(fieldNamesPanel, 2, false);
    add(fieldNamesPanel, BorderLayout.SOUTH);

  }

  public void addFieldName() {
    final String selectedFieldName = (String)fieldNamesField.getSelectedItem();
    if (StringUtils.hasText(selectedFieldName)) {
      final int position = filterField.getCaretPosition();
      final Document document = filterField.getDocument();
      try {
        document.insertString(position, " " + selectedFieldName + " ", null);
      } catch (final BadLocationException e) {
      }
      ((JComponent)filterField).requestFocusInWindow();
    }
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    super.addPropertyChangeListener(propertyName, listener);
    filterField.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public String getFieldValidationMessage() {
    return filterField.getFieldValidationMessage();
  }

  @Override
  public <T> T getFieldValue() {
    return filterField.getFieldValue();
  }

  @Override
  public boolean isFieldValid() {
    return filterField.isFieldValid();
  }

  @Override
  public void setFieldBackgroundColor(final Color color) {
    filterField.setFieldBackgroundColor(color);
  }

  @Override
  public void setFieldForegroundColor(final Color color) {
    filterField.setFieldForegroundColor(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    filterField.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    filterField.setFieldToolTip(toolTip);
  }

  @Override
  public void setFieldValid() {
    filterField.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    filterField.setFieldValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    filterField.setToolTipText(text);
  }

  @Override
  public void updateFieldValue() {
    filterField.updateFieldValue();
  }

}
