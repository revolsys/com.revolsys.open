package com.revolsys.swing.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class LambdaTableModelColumn<R, V> {

  private final String columnName;

  private final Class<?> columnClass;

  private final boolean editable;

  private final Function<R, V> getValueFunction;

  private final BiConsumer<R, V> setValueFunction;

  private TableCellEditor cellEditor;

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction) {
    this.columnName = columnName;
    this.columnClass = columnClass;
    this.editable = false;
    this.getValueFunction = getValueFunction;
    this.setValueFunction = null;
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction) {
    this.columnName = columnName;
    this.columnClass = columnClass;
    this.editable = true;
    this.getValueFunction = getValueFunction;
    this.setValueFunction = setValueFunction;
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction,
    final TableCellEditor cellEditor) {
    this.columnName = columnName;
    this.columnClass = columnClass;
    this.editable = true;
    this.getValueFunction = getValueFunction;
    this.setValueFunction = setValueFunction;
    this.cellEditor = cellEditor;
  }

  public void applyCellEditor(final TableColumn tableColumn) {
    if (this.cellEditor != null) {
      tableColumn.setCellEditor(this.cellEditor);
    }
  }

  public TableCellEditor getCellEditor() {
    return this.cellEditor;
  }

  public Class<?> getColumnClass() {
    return this.columnClass;
  }

  public String getColumnName() {
    return this.columnName;
  }

  public V getValue(final R row) {
    return this.getValueFunction.apply(row);
  }

  public boolean isEditable() {
    return this.editable;
  }

  public LambdaTableModelColumn<R, V> setCellEditor(final TableCellEditor editor) {
    this.cellEditor = editor;
    return this;
  }

  @SuppressWarnings("unchecked")
  public void setValue(final R row, final Object value) {
    this.setValueFunction.accept(row, (V)value);
  }

}
