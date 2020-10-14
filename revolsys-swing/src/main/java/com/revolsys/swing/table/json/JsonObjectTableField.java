package com.revolsys.swing.table.json;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.toolbar.ToolBar;

public class JsonObjectTableField extends ValueField {
  private static final long serialVersionUID = 1L;

  private final JsonObjectTableModel model;

  private final JButton addButton;

  private final ComboBox<FieldDefinition> addNamesField;

  private final List<FieldDefinition> fields = new ArrayList<>();

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
    setFields(fields);

    final TablePanel tablePanel = this.model.newTablePanel();
    add(tablePanel);

    this.table = tablePanel.getTable();
    this.table.setTerminateEditOnFocusLost(true);
    this.toolBar = tablePanel.getToolBar();

    this.addNamesField = ComboBox.newComboBox("name", this.fields,
      (field) -> ((FieldDefinition)field).getTitle());
    this.addNamesField.setMaximumSize(new Dimension(200, 50));

    this.toolBar.addComponent("add", this.addNamesField);

    this.addButton = this.toolBar.addButtonTitleIcon("add", "Add Field", "add", this::addRow);
    this.addButton.setEnabled(false);

    this.addNamesField.addItemListener((e) -> {
      this.addButton.setEnabled(this.addNamesField.getSelectedItem() != null);
    });
  }

  private void addRow() {
    try {
      JsonObject object = getFieldValue();
      if (object == null) {
        object = new JsonObjectHash();
      }
      final String name;
      final FieldDefinition field = this.addNamesField.getSelectedItem();
      name = field.getName();
      if (name != null) {
        object.put(name, null);
        setFieldValue(object);
      }
    } finally {
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
    if (this.addNamesField != null) {
      this.addNamesField.removeAllItems();
      final JsonObject object = getFieldValue();
      for (final FieldDefinition field : this.fields) {
        final String fieldName = field.getName();
        if (object == null || !object.containsKey(fieldName)) {
          this.addNamesField.addItem(field);
        }
      }
    }
  }

  public void setFields(final List<FieldDefinition> fields) {
    this.fields.clear();
    this.model.setFields(fields);
    for (final FieldDefinition field : fields) {
      this.fields.add(field);
    }
    refresh();
  }

  @Override
  public boolean setFieldValue(final Object value) {
    try {
      if (value == getFieldValue()) {
        this.model.setObject((JsonObject)value);
      } else if (value == null) {
        final boolean set = super.setFieldValue(null);
        this.model.setObject(null);
        return set;
      } else {
        JsonObject clone = Json.JSON_OBJECT.toObject(value);
        if (clone == value) {
          clone = clone.clone();
        }
        final boolean set = super.setFieldValue(clone);
        this.model.setObject(clone);
        return set;
      }
    } finally {
      refresh();
    }
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
