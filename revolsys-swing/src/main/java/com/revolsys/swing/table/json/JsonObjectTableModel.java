package com.revolsys.swing.table.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.table.TableColumnExt;
import org.jeometry.common.compare.NumericComparator;
import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
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

public class JsonObjectTableModel extends AbstractTableModel implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  public static BaseJTable newTable(final JsonObject object, final boolean editable) {
    final JsonObjectTableModel model = new JsonObjectTableModel(object, editable);
    return model.newTable();
  }

  private boolean editable;

  private final List<String> fieldNames = new ArrayList<>();

  private final Map<String, FieldDefinition> fieldByName = new HashMap<>();

  private JsonObject object;

  public JsonObjectTableModel(final boolean editable) {
    this(null, editable);
  }

  public JsonObjectTableModel(final JsonObject object, final boolean editable) {
    setObject(object);
    setEditable(editable);

    addMenuItem("edit", "Remove Row", "delete", this::removeRow);
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

  public JsonObject getObject() {
    return this.object;
  }

  public Object getObjectValue(final int rowIndex, final int columnIndex) {
    JsonObject object = this.object;
    if (object == null) {
      return "\u2026";
    } else {
      final String fieldName = getFieldName(rowIndex);
      return object.getValue(fieldName);
    }
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
        JsonObject object = this.object;
        if (object == null) {
          return "\u2026";
        } else {
          final String fieldName = getFieldName(rowIndex);
          return object.getValue(fieldName);
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
        column.setCellEditor(cellEditor);
      }
    }
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  public void refreshFieldNames() {
    this.fieldNames.clear();
    final JsonObject object = this.object;
    if (object != null) {
      this.fieldNames.addAll(object.keySet());
    }
    fireTableDataChanged();
  }

  private void removeRow(final int rowIndex, final int columnIndex) {
    final String propertyName = getFieldName(rowIndex);
    final JsonObject object = this.object;
    if (object != null) {
      object.remove(propertyName);
    }
    refreshFieldNames();
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

  public void setObject(final JsonObject object) {
    this.object = object;
    refreshFieldNames();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final JsonObject object = this.object;
    if (object != null) {
      if (columnIndex == 2) {
        final FieldDefinition field = getField(rowIndex);
        final String fieldName = field.getName();
        final Object oldValue = object.put(fieldName, value);
        if (!DataType.equal(value, oldValue)) {
          fireTableCellUpdated(rowIndex, columnIndex);
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
    final CodeTable codeTable = field.getCodeTable();
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
