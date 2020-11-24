package com.revolsys.swing.table.json;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ArrayListComboBoxModel;
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

  private final boolean removeEmptyValues = true;

  public JsonObjectTableField(final FieldDefinition field, final List<FieldDefinition> fields) {
    super(new VerticalLayout(), field.getName(), null);
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
      (customField) -> ((FieldDefinition)customField).getTitle());
    this.addNamesField.setMaximumSize(new Dimension(200, 50));

    this.toolBar.addComponent("add", this.addNamesField);

    this.addButton = this.toolBar.addButtonTitleIcon("add", "Add Field", "add", this::addRow);
    this.addButton.setEnabled(false);

    this.addNamesField.addItemListener((e) -> {
      this.addButton.setEnabled(this.addNamesField.getSelectedItem() != null);
    });

  }

  private void addRow() {
    int editRow = -1;
    JsonObject jsonObject = getFieldValue();
    final FieldDefinition field = this.addNamesField.getSelectedItem();
    if (field != null) {
      final ArrayListComboBoxModel<FieldDefinition> namesModel = (ArrayListComboBoxModel<FieldDefinition>)this.addNamesField
        .getModel();
      namesModel.removeElement(field);
      final String name = field.getName();
      if (name != null) {
        if (jsonObject == null) {
          jsonObject = JsonObject.tree();
        } else {
          jsonObject = jsonObject.clone();
        }
        jsonObject.addValue(name, null);
        setFieldValue(jsonObject);
        final int row = this.model.getFieldIndex(name);
        if (row != -1) {
          editRow = this.table.convertRowIndexToModel(row);
        }
      }
    }
    if (editRow != -1) {
      this.table.editCellAt(editRow, 2);
    }
  }

  public void cancelCellEditing() {
    this.table.cancelCellEditing();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public boolean isRemoveEmptyValues() {
    return this.removeEmptyValues;
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
        this.model.setJsonObject((JsonObject)value);
      } else if (value == null) {
        final boolean set = super.setFieldValue(null);
        this.model.setJsonObject(null);
        return set;
      } else {
        final JsonObject jsonObject = Json.JSON_OBJECT.toObject(value);
        final boolean set = super.setFieldValue(jsonObject);
        this.model.setJsonObject(jsonObject);
        return set;
      }
    } finally {
      refresh();
    }
    return false;
  }

  public void setRemoveEmptyProperties(final boolean removeEmptyProperties) {
    this.model.setRemoveEmptyProperties(removeEmptyProperties);
  }

  public boolean stopCellEditing() {
    return this.table.stopCellEditing();
  }

  private void tableChanged(final TableModelEvent e) {
    final JsonObject oldValue = getFieldValue();
    final JsonObject value = this.model.getJsonObject();
    if (oldValue != value) {
      setFieldValue(value);
    }
    refresh();
  }
}
