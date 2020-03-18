package com.revolsys.swing.table.json;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataType;

import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.toolbar.ToolBar;

public class JsonObjectTableField extends ValueField {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final JsonObjectTableModel model;

  private String addNameValue;

  private final JButton addButton;

  private final DocumentListener addNameDocumentListener = new DocumentListener() {

    @Override
    public void changedUpdate(final DocumentEvent e) {
      handle();
    }

    private void handle() {
      final String name = JsonObjectTableField.this.addNameField.getText();
      JsonObjectTableField.this.addNameValue = name;
      final JsonObject fieldValue = getFieldValue();
      final boolean enabled = name.length() > 0
        && (fieldValue == null || !fieldValue.containsKey(name));
      JsonObjectTableField.this.addButton.setEnabled(enabled);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
      handle();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
      handle();
    }
  };

  private final TextField addNameField;

  private final ComboBox<String> addNamesField;

  private final List<String> fieldNames = new ArrayList<>();

  private final ToolBar toolBar;

  private final BaseJTable table;

  public JsonObjectTableField(final String fieldName) {
    this(fieldName, Collections.emptyList());
  }

  public JsonObjectTableField(final String fieldName, final List<FieldDefinition> fields) {
    super(new VerticalLayout(), fieldName, null);
    setOpaque(false);

    this.model = new JsonObjectTableModel(true);
    this.model.addTableModelListener(this::tableChanged);

    final TablePanel tablePanel = this.model.newTablePanel();
    add(tablePanel);

    this.table = tablePanel.getTable();
    this.toolBar = tablePanel.getToolBar();
    this.addNameField = new TextField("name", 15);
    this.addNameField.setMaximumSize(new Dimension(200, 50));
    this.addNameField.getDocument().addDocumentListener(this.addNameDocumentListener);
    this.toolBar.addComponent("add", this.addNameField);

    this.addNamesField = ComboBox.newComboBox("name", this.fieldNames);
    this.addNamesField.setMaximumSize(new Dimension(200, 50));

    this.toolBar.addComponent("add", this.addNamesField);

    this.addButton = this.toolBar.addButtonTitleIcon("add", "Add Field", "add", this::addRow);
    this.addButton.setEnabled(false);

    this.addNamesField.addItemListener((e) -> {
      this.addButton.setEnabled(this.addNamesField.getSelectedItem() != null);
    });
    setFields(fields);
  }

  private void addRow() {
    JsonObject object = getFieldValue();
    if (object == null) {
      object = new JsonObjectHash();
    }
    final String name;
    if (this.fieldNames.isEmpty()) {
      name = this.addNameValue;
      this.addNameField.setText("");
    } else {
      name = this.addNamesField.getSelectedItem();
    }
    if (name != null) {
      object.put(name, null);
      setFieldValue(object);

      refresh();
    }
  }

  public void cancelCellEditing() {
    this.table.cancelCellEditing();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  private void refresh() {
    if (!this.fieldNames.isEmpty()) {
      this.addNamesField.removeAllItems();
      final JsonObject object = getFieldValue();
      for (final String fieldName : this.fieldNames) {
        if (object == null || !object.containsKey(fieldName)) {
          this.addNamesField.addItem(fieldName);
        }
      }
    }
  }

  public void setFields(final List<FieldDefinition> fields) {
    this.fieldNames.clear();
    for (final FieldDefinition field : fields) {
      final String name = field.getName();
      this.fieldNames.add(name);
      final DataType dataType = field.getDataType();
      this.model.setDataType(name, dataType);
    }
    this.addNameField.setVisible(this.fieldNames.isEmpty());
    this.addNamesField.setVisible(!this.fieldNames.isEmpty());
    this.refresh();
  }

  @Override
  public boolean setFieldValue(final Object value) {
    if (value != getFieldValue()) {
      final JsonObject clone = Json.clone(value);
      this.model.setObject(clone);
      return super.setFieldValue(clone);
    }
    refresh();
    return false;
  }

  public boolean stopCellEditing() {
    return this.table.stopCellEditing();
  }

  private void tableChanged(final TableModelEvent e) {
    if (e.getType() == TableModelEvent.UPDATE) {
      final String fieldName = getFieldName();
      final Object fieldValue = Json.clone(getFieldValue());
      firePropertyChange(fieldName, JsonObject.EMPTY, fieldValue);

    }
    refresh();
  }
}
