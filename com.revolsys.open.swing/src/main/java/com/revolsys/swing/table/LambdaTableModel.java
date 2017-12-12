package com.revolsys.swing.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.revolsys.swing.menu.BaseJPopupMenu;

public abstract class LambdaTableModel<R> extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private final List<LambdaTableModelColumn<R, ?>> columns = new ArrayList<>();

  private List<R> rows = new ArrayList<>();

  public LambdaTableModel() {
  }

  public LambdaTableModel(final List<R> rows) {
    super();
    this.rows = rows;
  }

  protected LambdaTableModelColumn<R, ?> addColumn(final LambdaTableModelColumn<R, ?> column) {
    this.columns.add(column);
    return column;
  }

  protected <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction);
    this.columns.add(column);
    return column;
  }

  protected <V> LambdaTableModelColumn<R, ?> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction);
    this.columns.add(column);
    return column;
  }

  protected <V> LambdaTableModelColumn<R, ?> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction, final TableCellEditor cellEditor) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction, cellEditor);
    this.columns.add(column);
    return column;
  }

  public void addRow(final R row) {
    final int rowCount = this.rows.size();
    this.rows.add(row);
    fireTableRowsInserted(rowCount, rowCount);
  }

  public void applyTableCellEditors(final BaseJTable table) {
    for (int i = 0; i < this.columns.size(); i++) {
      final TableColumn tableColumn = table.getColumnExt(i);
      final LambdaTableModelColumn<R, ?> column = getColumn(i);
      column.applyCellEditor(tableColumn);
    }
  }

  protected LambdaTableModelColumn<R, ?> getColumn(final int columnIndex) {
    return this.columns.get(columnIndex);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return getColumn(columnIndex).getColumnClass();
  }

  @Override
  public int getColumnCount() {
    return this.columns.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return getColumn(columnIndex).getColumnName();
  }

  @Override
  public BaseJPopupMenu getHeaderMenu(final int columnIndex) {
    return getColumn(columnIndex).getHeaderMenu();
  }

  public R getRow(final int rowIndex) {
    return this.rows.get(rowIndex);
  }

  @Override
  public int getRowCount() {
    return this.rows.size();
  }

  public List<R> getRows() {
    return this.rows;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final R row = getRow(rowIndex);
    return getColumn(columnIndex).getValue(row);
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return getColumn(columnIndex).isEditable();
  }

  protected void setRows(final List<R> rows) {
    this.rows = rows;
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final R row = getRow(rowIndex);
    getColumn(columnIndex).setValue(row, value);
  }

}
