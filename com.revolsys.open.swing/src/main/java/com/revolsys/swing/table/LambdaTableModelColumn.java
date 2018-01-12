package com.revolsys.swing.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.revolsys.swing.map.layer.elevation.gridded.ColorTableCellRenderer;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.function.Function3;

public class LambdaTableModelColumn<R, V> {

  private final String columnName;

  private final Class<?> columnClass;

  private final boolean editable;

  private final Function<R, V> getValueFunction;

  private Function3<Integer, Integer, R, V> getValueIndexFunction;

  private final BiConsumer<R, V> setValueFunction;

  private TableCellEditor cellEditor;

  private TableCellRenderer cellRenderer;

  private MenuFactory headerMenuFactory;

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass) {
    this(columnName, columnClass, null);
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction) {
    this(columnName, columnClass, getValueFunction, null);
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction) {
    this(columnName, columnClass, getValueFunction, setValueFunction, null, null);
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction,
    final Function<V, ? extends Object> renderFunction) {
    this(columnName, columnClass, getValueFunction, setValueFunction, renderFunction, null);
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction,
    final Function<V, ? extends Object> renderFunction, final TableCellEditor cellEditor) {
    this.columnName = columnName;
    this.columnClass = columnClass;
    this.editable = setValueFunction != null;
    this.getValueFunction = getValueFunction;
    this.setValueFunction = setValueFunction;
    if (renderFunction != null) {
      this.cellRenderer = new LambdaCellRenderer<>(renderFunction);
    }
    this.cellEditor = cellEditor;
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction,
    final TableCellEditor cellEditor) {
    this(columnName, columnClass, getValueFunction, setValueFunction, null, cellEditor);
  }

  public void applySettings(final TableColumn tableColumn) {
    if (this.cellEditor != null) {
      tableColumn.setCellEditor(this.cellEditor);
    }
    if (this.cellRenderer != null) {
      tableColumn.setCellRenderer(this.cellRenderer);
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

  public BaseJPopupMenu getHeaderMenu() {
    if (this.headerMenuFactory == null) {
      return null;
    } else {
      return this.headerMenuFactory.newJPopupMenu();
    }
  }

  public synchronized MenuFactory getHeaderMenuFactory() {
    if (this.headerMenuFactory == null) {
      this.headerMenuFactory = new MenuFactory(this.columnName);
    }
    return this.headerMenuFactory;
  }

  public V getValue(final int rowIndex, final int columnIndex, final R row) {
    if (this.getValueFunction == null) {
      if (this.getValueIndexFunction == null) {
        return null;
      } else {
        return this.getValueIndexFunction.apply(rowIndex, columnIndex, row);
      }
    } else {
      return this.getValueFunction.apply(row);
    }
  }

  public boolean isEditable() {
    return this.editable;
  }

  public LambdaTableModelColumn<R, V> setCellEditor(final TableCellEditor editor) {
    this.cellEditor = editor;
    return this;
  }

  public void setCellRenderer(final ColorTableCellRenderer cellRenderer) {
    this.cellRenderer = cellRenderer;
  }

  public LambdaTableModelColumn<R, V> setGetValueIndexFunction(
    final Function3<Integer, Integer, R, V> getValueIndexFunction) {
    this.getValueIndexFunction = getValueIndexFunction;
    return this;
  }

  @SuppressWarnings("unchecked")
  public void setValue(final R row, final Object value) {
    this.setValueFunction.accept(row, (V)value);
  }

  public void withHeaderMenu(final BiConsumer<LambdaTableModelColumn<R, V>, MenuFactory> action) {
    final MenuFactory headerMenuFactory = getHeaderMenuFactory();
    action.accept(this, headerMenuFactory);
  }

}
