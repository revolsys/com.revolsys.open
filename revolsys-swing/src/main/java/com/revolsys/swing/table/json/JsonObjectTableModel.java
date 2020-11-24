package com.revolsys.swing.table.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.table.TableColumnExt;
import org.jeometry.common.compare.NumericComparator;
import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.util.Strings;

public class JsonObjectTableModel extends AbstractTableModel
  implements PropertyChangeSupportProxy, CellEditorListener {

  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  private boolean editable;

  private final List<String> fieldNames = new ArrayList<>();

  private final Map<String, FieldDefinition> fieldByName = new HashMap<>();

  private JsonObject jsonObject;

  private boolean removeEmptyProperties = true;

  public JsonObjectTableModel(final boolean editable) {
    this(null, editable);
  }

  public JsonObjectTableModel(final JsonObject object, final boolean editable) {
    setJsonObject(object);
    setEditable(editable);

    addMenuItem("edit", "Remove Row", "delete", this::removeRow);
  }

  @Override
  public void editingCanceled(final ChangeEvent e) {
    removeEmptyProperties();

  }

  @Override
  public void editingStopped(final ChangeEvent e) {
    removeEmptyProperties();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public JComponent getEditorField(final int rowIndex, final int columnIndex, final Object value) {
    final String fieldName = getFieldName(rowIndex);
    final FieldDefinition field = this.fieldByName.get(fieldName);
    if (field == null) {
      return SwingUtil.newField(DataTypes.STRING, fieldName, value);
    } else {
      return SwingUtil.newField(field, true);
    }
  }

  public FieldDefinition getField(final int rowIndex) {
    final String fieldName = getFieldName(rowIndex);
    return this.fieldByName.get(fieldName);
  }

  public int getFieldIndex(final String fieldName) {
    return this.fieldNames.indexOf(fieldName);
  }

  public String getFieldName(final int rowIndex) {
    return this.fieldNames.get(rowIndex);
  }

  public String getFieldTitle(final int rowIndex) {
    final String fieldName = getFieldName(rowIndex);
    final FieldDefinition field = this.fieldByName.get(fieldName);
    if (field == null) {
      return fieldName;
    } else {
      return field.getTitle();
    }
  }

  public JsonObject getJsonObject() {
    return this.jsonObject;
  }

  @Override
  public int getRowCount() {
    return this.fieldNames.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return rowIndex;
      case 1:
        return getFieldTitle(rowIndex);
      case 2: {
        final JsonObject jsonObject = this.jsonObject;
        if (jsonObject == null) {
          return "\u2026";
        } else {
          final String fieldName = getFieldName(rowIndex);
          return jsonObject.getValue(fieldName);
        }
      }
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return columnIndex == 2;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isRemoveEmptyProperties() {
    return this.removeEmptyProperties;
  }

  public void loadCodeTable(final CodeTable codeTable) {
    if (codeTable.isLoadAll() && !codeTable.isLoaded()) {
      codeTable.refreshIfNeeded();
      fireTableDataChanged();
    }
  }

  @Override
  public BaseJTable newTable() {
    final BaseJTable table = new BaseJTable(this);
    table.setVisibleRowCount(Math.min(8, this.fieldByName.size()));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setAutoCreateColumnsFromModel(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    final JsonObjectTableCellRenderer cellRenderer = new JsonObjectTableCellRenderer();

    for (int i = 0; i < 3; i++) {
      final TableColumnExt column = table.getColumnExt(i);
      column.setCellRenderer(cellRenderer);
      if (i == 0) {
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(40);
        column.setComparator(new NumericComparator<Integer>());
      } else if (i == 1) {
        column.setPreferredWidth(150);
      } else {
        column.setPreferredWidth(210);
        final BaseTableCellEditor cellEditor = new BaseTableCellEditor(table);
        cellEditor.addCellEditorListener(this);
        column.setCellEditor(cellEditor);
      }
    }
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  public void refreshFieldNames() {
    this.fieldNames.clear();
    final JsonObject jsonObject = this.jsonObject;
    if (jsonObject != null) {
      this.fieldNames.addAll(jsonObject.keySet());
    }
    fireTableDataChanged();
  }

  public void removeEmptyProperties() {
    final JsonObject jsonObject = this.jsonObject;
    if (this.removeEmptyProperties) {
      final JsonObject cleanedJsonObject = jsonObject.withNonEmptyValues();
      if (cleanedJsonObject != jsonObject) {
        setJsonObject(cleanedJsonObject);
      }
    }
  }

  private void removeRow(final int rowIndex, final int columnIndex) {
    final String propertyName = getFieldName(rowIndex);
    final JsonObject jsonObject = this.jsonObject;
    if (jsonObject != null && jsonObject.containsKey(propertyName)) {
      JsonObject updatedJsonObject = jsonObject.clone();
      updatedJsonObject.remove(propertyName);
      if (this.removeEmptyProperties) {
        updatedJsonObject = updatedJsonObject.withNonEmptyValues();
      }
      setJsonObject(updatedJsonObject);
    }
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public JsonObjectTableModel setFields(final List<FieldDefinition> fields) {
    this.fieldByName.clear();
    if (fields != null) {
      for (final FieldDefinition field : fields) {
        final String name = field.getName();
        this.fieldByName.put(name, field);
      }
    }
    return this;
  }

  public void setJsonObject(final JsonObject jsonObject) {
    if (this.jsonObject != jsonObject) {
      this.jsonObject = jsonObject;
      refreshFieldNames();
    }
  }

  public void setRemoveEmptyProperties(final boolean removeEmptyProperties) {
    this.removeEmptyProperties = removeEmptyProperties;
  }

  @Override
  public void setValueAt(Object value, final int rowIndex, final int columnIndex) {
    JsonObject jsonObject = this.jsonObject;
    if (jsonObject != null) {
      if (columnIndex == 2) {
        final FieldDefinition field = getField(rowIndex);
        final String fieldName = field.getName();
        if (!jsonObject.equalValue(fieldName, value)) {
          value = field.toFieldValue(value);
          jsonObject = jsonObject//
            .clone()
            .addValue(fieldName, value);
          setJsonObject(jsonObject);
        }
      }
    }
  }

  public String toDisplayValue(final int fieldIndex, final Object objectValue) {
    String text;
    final FieldDefinition field = getField(fieldIndex);
    if (objectValue instanceof Geometry) {
      final Geometry geometry = (Geometry)objectValue;
      return geometry.getGeometryType();
    }
    CodeTable codeTable = null;
    if (field != null) {
      codeTable = field.getCodeTable();
    }
    if (codeTable == null) {
      text = DataTypes.toString(objectValue);
    } else {
      if (!codeTable.isLoadAll() || codeTable.isLoaded()) {
        final List<Object> values = codeTable.getValues(Identifier.newIdentifier(objectValue));
        if (values == null || values.isEmpty()) {
          text = DataTypes.toString(objectValue);
        } else if (values.size() == 1) {
          final Object codeValue = values.get(0);
          if (codeValue instanceof Code) {
            text = ((Code)codeValue).getDescription();
          } else {
            text = DataTypes.toString(codeValue);
          }
        } else {
          text = Strings.toString(values);
        }
      } else {
        if (!codeTable.isLoading()) {
          final CodeTable tableToLoad = codeTable;
          Invoke.background("Load " + codeTable, () -> loadCodeTable(tableToLoad));
        }
        text = "...";
      }
    }
    if (text == null || text.length() == 0) {
      text = "-";
    }
    return text;
  }
}
