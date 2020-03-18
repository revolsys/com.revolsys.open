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
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.editor.BaseTableCellEditor;

public class JsonObjectTableModel extends AbstractTableModel implements PropertyChangeSupportProxy {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  public static BaseJTable newTable(final JsonObject object, final boolean editable) {
    final JsonObjectTableModel model = new JsonObjectTableModel(object, editable);
    return model.newTable();
  }

  private boolean editable;

  private final List<String> propertyNames = new ArrayList<>();

  private JsonObject object;

  private final Map<String, DataType> dataTypeByPropertyName = new HashMap<>();

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
    final DataType dataType = getPropertyType(rowIndex);
    final String propertyName = getPropertyName(rowIndex);
    return SwingUtil.newField(dataType, propertyName, value);
  }

  public JsonObject getObject() {
    return this.object;
  }

  public Object getObjectValue(final int rowIndex, final int columnIndex) {
    if (this.object == null) {
      return "\u2026";
    } else {
      final String propertyName = getPropertyTitle(rowIndex);
      return this.object.getValue(propertyName);
    }
  }

  public String getPropertyName(final int rowIndex) {
    return this.propertyNames.get(rowIndex);
  }

  public List<String> getPropertyNames() {
    return this.propertyNames;
  }

  public String getPropertyTitle(final int rowIndex) {
    return this.propertyNames.get(rowIndex);
  }

  public DataType getPropertyType(final int rowIndex) {
    final String propertyName = getPropertyName(rowIndex);
    return this.dataTypeByPropertyName.getOrDefault(propertyName, DataTypes.OBJECT);
  }

  @Override
  public int getRowCount() {
    return this.propertyNames.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return rowIndex;
      case 1:
        return getPropertyTitle(rowIndex);
      case 2:
        return getObjectValue(rowIndex, columnIndex);
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

  @Override
  public BaseJTable newTable() {
    final BaseJTable table = new BaseJTable(this);
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
    this.propertyNames.clear();
    if (this.object != null) {
      this.propertyNames.addAll(this.object.keySet());
    }
    fireTableDataChanged();
  }

  private void removeRow(final int rowIndex, final int columnIndex) {
    final String propertyName = getPropertyName(rowIndex);
    this.object.remove(propertyName);
    refreshFieldNames();
  }

  public void setDataType(final String name, final DataType dataType) {
    this.dataTypeByPropertyName.put(name, dataType);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setObject(final JsonObject object) {
    this.object = object;
    refreshFieldNames();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    if (this.object != null) {
      if (columnIndex == 2) {
        final String propertyName = getPropertyName(rowIndex);
        final Object oldValue = this.object.put(propertyName, value);
        if (!DataType.equal(value, oldValue)) {
          fireTableCellUpdated(rowIndex, columnIndex);
        }
      }
    }
  }

}
