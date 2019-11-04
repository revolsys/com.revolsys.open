package com.revolsys.swing.table.lambda.column;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.table.TableColumn;

import com.revolsys.record.Record;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.util.Property;

public class ColumnBasedTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private final List<TableModelColumn> columns = new ArrayList<>();

  private int rowCount;

  public ColumnBasedTableModel() {
  }

  public ColumnBasedTableModel addColumn(final TableModelColumn column) {
    this.columns.add(column);
    fireTableStructureChanged();
    return this;
  }

  public ColumnBasedTableModel addColumnRecord(final String columnName, final Record record,
    final boolean editable) {
    final TableModelColumn column = new RecordTableModelColumn(columnName, record, editable);
    return addColumn(column);
  }

  public ColumnBasedTableModel addColumnRowIndex() {
    final TableModelColumn column = new RowIndexTableModelColumn();
    return addColumn(column);
  }

  public <V> ColumnBasedTableModel addColumnValues(final String columnName,
    final Class<? super V> columnClass, final List<V> values) {
    final TableModelColumn column = new RowValuesTableModelColumn<>(columnName, columnClass,
      values);
    return addColumn(column);
  }

  public void applyTableColumnSettings(final BaseJTable table) {
    for (int i = 0; i < this.columns.size(); i++) {
      final TableColumn tableColumn = table.getColumnExt(i);
      final TableModelColumn column = getColumn(i);
      column.applySettings(table, tableColumn);
    }
  }

  public TableModelColumn getColumn(final int columnIndex) {
    return this.columns.get(columnIndex);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column == null) {
      return Object.class;
    } else {
      return column.getColumnClass();
    }
  }

  @Override
  public int getColumnCount() {
    return this.columns.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column == null) {
      return null;
    } else {
      return column.getColumnName();
    }
  }

  @Override
  public MenuFactory getHeaderMenuFactory(final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column == null) {
      return null;
    } else {
      return column.getHeaderMenuFactory();
    }
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    BaseJPopupMenu menu = null;
    final TableModelColumn column = getColumn(columnIndex);
    if (column != null) {
      menu = column.getMenu(rowIndex);
    }
    if (menu == null) {
      menu = super.getMenu(rowIndex, columnIndex);
    }
    if (menu.getComponentCount() > 0) {
      menu.addSeparator();
    }
    final Object value = getValueAt(rowIndex, columnIndex);

    final boolean canCopy = Property.hasValue(value);
    final BaseJTable table = getTable();
    final boolean cellEditable = isCellEditable(rowIndex, columnIndex);
    if (cellEditable) {
      final JMenuItem cutMenu = RunnableAction.newMenuItem("Cut Field Value", "cut",
        table::cutFieldValue);
      cutMenu.setEnabled(canCopy);
      menu.add(cutMenu);
    }

    final JMenuItem copyMenu = RunnableAction.newMenuItem("Copy Field Value", "page_copy",
      table::copyFieldValue);
    copyMenu.setEnabled(canCopy);
    menu.add(copyMenu);

    if (cellEditable) {
      menu.add(
        RunnableAction.newMenuItem("Paste Field Value", "paste_plain", table::pasteFieldValue));
    }
    return menu;
  }

  @Override
  public int getRowCount() {
    return this.rowCount;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column == null) {
      return null;
    } else {
      return column.getValueAt(rowIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column == null) {
      return false;
    } else {
      return column.isCellEditable(rowIndex);
    }
  }

  @Override
  public BaseJTable newTable() {
    final BaseJTable table = super.newTable();
    applyTableColumnSettings(table);
    return table;
  }

  public ColumnBasedTableModel setRowCount(final int rowCount) {
    this.rowCount = rowCount;
    return this;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final TableModelColumn column = getColumn(columnIndex);
    if (column != null) {
      column.setValueAt(value, rowIndex);
    }
  }

}
