package com.revolsys.swing.table.lambda.column;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJTable;

public class AbstractTableModelColumn implements TableModelColumn {

  private String columnName;

  private Class<?> columnClass;

  private boolean editable;

  private Function<BaseJTable, TableCellEditor> cellEditorConstructor;

  private Function<BaseJTable, TableCellRenderer> cellRendererConstructor;

  private MenuFactory headerMenuFactory;

  private MenuFactory menuFactory;

  private int columnIndex;

  public AbstractTableModelColumn() {
  }

  public AbstractTableModelColumn(final String columnName, final Class<?> columnClass) {
    this.columnName = columnName;
    this.columnClass = columnClass;
  }

  public AbstractTableModelColumn(final String columnName, final Class<?> columnClass,
    final boolean editable) {
    this.columnName = columnName;
    this.columnClass = columnClass;
    this.editable = editable;
  }

  @Override
  public void applySettings(final BaseJTable table, final TableColumn tableColumn) {
    if (this.cellEditorConstructor != null) {
      tableColumn.setCellEditor(this.cellEditorConstructor.apply(table));
    }
    if (this.cellRendererConstructor != null) {
      tableColumn.setCellRenderer(this.cellRendererConstructor.apply(table));
    }
  }

  @Override
  public Class<?> getColumnClass() {
    return this.columnClass;
  }

  public int getColumnIndex() {
    return this.columnIndex;
  }

  @Override
  public String getColumnName() {
    return this.columnName;
  }

  @Override
  public synchronized MenuFactory getHeaderMenuFactory() {
    if (this.headerMenuFactory == null) {
      this.headerMenuFactory = new MenuFactory(this.columnName);
    }
    return this.headerMenuFactory;
  }

  @Override
  public BaseJPopupMenu getMenu() {
    if (this.menuFactory == null) {
      return null;
    } else {
      return this.menuFactory.newJPopupMenu();
    }
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex) {
    return getMenu();
  }

  public synchronized MenuFactory getMenuFactory() {
    if (this.menuFactory == null) {
      this.menuFactory = new MenuFactory(this.columnName);
    }
    return this.menuFactory;
  }

  @Override
  public Object getValueAt(final int rowIndex) {
    return null;
  }

  @Override
  public boolean isCellEditable(final int rowIndex) {
    return this.editable;
  }

  public AbstractTableModelColumn setCellEditor(
    final Function<BaseJTable, TableCellEditor> editor) {
    this.cellEditorConstructor = editor;
    return this;
  }

  public AbstractTableModelColumn setCellEditor(final TableCellEditor editor) {
    this.cellEditorConstructor = table -> editor;
    return this;
  }

  public AbstractTableModelColumn setCellRenderer(
    final Function<BaseJTable, TableCellRenderer> cellRenderer) {
    this.cellRendererConstructor = cellRenderer;
    return this;
  }

  public AbstractTableModelColumn setCellRenderer(final TableCellRenderer cellRenderer) {
    this.cellRendererConstructor = table -> cellRenderer;
    return this;
  }

  public void setColumnIndex(final int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public AbstractTableModelColumn setMenuFactory(final MenuFactory menuFactory) {
    this.menuFactory = menuFactory;
    return this;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex) {
  }

  public AbstractTableModelColumn withHeaderMenu(
    final BiConsumer<AbstractTableModelColumn, MenuFactory> action) {
    final MenuFactory headerMenuFactory = getHeaderMenuFactory();
    action.accept(this, headerMenuFactory);
    return this;
  }

  public AbstractTableModelColumn withMenu(
    final BiConsumer<AbstractTableModelColumn, MenuFactory> action) {
    final MenuFactory menuFactory = getMenuFactory();
    action.accept(this, menuFactory);
    return this;
  }
}
