package com.revolsys.swing.table.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;

public class LambdaRowTableModel<R> extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private final List<LambdaTableModelColumn<R, ?>> columns = new ArrayList<>();

  private List<R> rows = new ArrayList<>();

  public LambdaRowTableModel() {
  }

  public LambdaRowTableModel(final List<R> rows) {
    super();
    this.rows = rows;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final LambdaTableModelColumn<R, V> column) {
    final int columnIndex = this.columns.size();
    this.columns.add(column);
    column.setColumnIndex(columnIndex);
    return column;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction);
    this.columns.add(column);
    return column;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction);
    this.columns.add(column);
    return column;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction, final LambdaStringValue<V> renderFunction) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction, renderFunction);
    this.columns.add(column);
    return column;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction, final LambdaStringValue<V> renderFunction,
    final TableCellEditor cellEditor) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction, renderFunction, cellEditor);
    this.columns.add(column);
    return column;
  }

  public <V> LambdaTableModelColumn<R, V> addColumn(final String columnName,
    final Class<?> columnClass, final Function<R, V> getValueFunction,
    final BiConsumer<R, V> setValueFunction, final TableCellEditor cellEditor) {
    final LambdaTableModelColumn<R, V> column = new LambdaTableModelColumn<>(columnName,
      columnClass, getValueFunction, setValueFunction, cellEditor);
    this.columns.add(column);
    return column;
  }

  public LambdaTableModelColumn<R, Integer> addColumnIndex() {
    final LambdaTableModelColumn<R, Integer> column = new LambdaTableModelColumn<>("index",
      Integer.TYPE);
    column.setGetValueIndexFunction((rowIndex, columnIndex, row) -> rowIndex);
    addColumn(column);
    return column;
  }

  public void addRow(final R row) {
    final int rowCount = this.rows.size();
    this.rows.add(row);
    fireTableRowsInserted(rowCount, rowCount);
  }

  public void applyTableColumnSettings(final BaseJTable table) {
    for (int i = 0; i < this.columns.size(); i++) {
      final TableColumn tableColumn = table.getColumnExt(i);
      final LambdaTableModelColumn<R, ?> column = getColumn(i);
      column.applySettings(tableColumn);
    }
  }

  public LambdaTableModelColumn<R, ?> getColumn(final int columnIndex) {
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
  public MenuFactory getHeaderMenuFactory(final int columnIndex) {
    return getColumn(columnIndex).getHeaderMenuFactory();
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
    final LambdaTableModelColumn<R, ?> column = getColumn(columnIndex);
    return column.getValue(rowIndex, columnIndex, row);
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return getColumn(columnIndex).isEditable();
  }

  @Override
  public BaseJTable newTable() {
    final BaseJTable table = super.newTable();
    applyTableColumnSettings(table);
    return table;
  }

  public void setRows(final List<R> rows) {
    this.rows = rows;
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final R row = getRow(rowIndex);
    final LambdaTableModelColumn<R, ?> column = getColumn(columnIndex);
    column.setValue(rowIndex, columnIndex, row, value);
  }

  public <V> void setValues(final LambdaTableModelColumn<R, V> column, final V value) {
    final int rowCount = getRowCount();
    final int columnIndex = column.getColumnIndex();
    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      final R row = getRow(rowIndex);
      column.setValue(rowIndex, columnIndex, row, value);
    }
  }

}
