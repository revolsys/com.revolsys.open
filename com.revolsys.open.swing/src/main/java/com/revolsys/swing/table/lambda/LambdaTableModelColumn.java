package com.revolsys.swing.table.lambda;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.Consumer4;
import org.jeometry.common.function.Function3;

import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;

public class LambdaTableModelColumn<R, V> {

  private final String columnName;

  private final Class<?> columnClass;

  private boolean editable;

  private final Function<R, V> getValueFunction;

  private Function3<Integer, Integer, R, V> getValueIndexFunction;

  private final BiConsumer<R, V> setValueFunction;

  private TableCellEditor cellEditor;

  private TableCellRenderer cellRenderer;

  private MenuFactory headerMenuFactory;

  private Consumer4<Integer, Integer, R, V> setValueIndexFunction;

  private int columnIndex;

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
    final LambdaStringValue<V> renderFunction) {
    this(columnName, columnClass, getValueFunction, setValueFunction, renderFunction, null);
  }

  public LambdaTableModelColumn(final String columnName, final Class<?> columnClass,
    final Function<R, V> getValueFunction, final BiConsumer<R, V> setValueFunction,
    final LambdaStringValue<V> renderFunction, final TableCellEditor cellEditor) {
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

  public int getColumnIndex() {
    return this.columnIndex;
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

  @SuppressWarnings("unchecked")
  public LambdaTableModelColumn<R, V> setAlignment(final int alignment) {
    if (this.cellRenderer == null) {
      this.cellRenderer = new DefaultTableRenderer(null, alignment);
    } else if (this.cellRenderer instanceof LambdaCellRenderer) {
      LambdaCellRenderer<V> lambdaRenderer = (LambdaCellRenderer<V>)this.cellRenderer;
      final LambdaStringValue<V> renderFunction = lambdaRenderer.getRenderFunction();
      lambdaRenderer = new LambdaCellRenderer<>(renderFunction, alignment);
    } else {
      throw new IllegalArgumentException("Cannot change alignment");
    }
    return this;
  }

  public LambdaTableModelColumn<R, V> setCellEditor(final TableCellEditor editor) {
    this.cellEditor = editor;
    return this;
  }

  public void setCellRenderer(final TableCellRenderer cellRenderer) {
    this.cellRenderer = cellRenderer;
  }

  public void setColumnIndex(final int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public LambdaTableModelColumn<R, V> setGetValueIndexFunction(
    final Function3<Integer, Integer, R, V> getValueIndexFunction) {
    this.getValueIndexFunction = getValueIndexFunction;
    return this;
  }

  public LambdaTableModelColumn<R, V> setSetValueIndexFunction(
    final Consumer4<Integer, Integer, R, V> setValueIndexFunction) {
    this.setValueIndexFunction = setValueIndexFunction;
    this.editable = this.setValueIndexFunction != null || this.setValueFunction != null;
    return this;
  }

  @SuppressWarnings("unchecked")
  public void setValue(final int rowIndex, final int columnIndex, final R row, final Object value) {
    if (this.setValueFunction == null) {
      if (this.setValueIndexFunction != null) {
        this.setValueIndexFunction.accept(rowIndex, columnIndex, row, (V)value);
      }
    } else {
      final V objectValue = DataTypes.toObject(this.columnClass, value);
      this.setValueFunction.accept(row, objectValue);
    }
  }

  public LambdaTableModelColumn<R, V> withHeaderMenu(
    final BiConsumer<LambdaTableModelColumn<R, V>, MenuFactory> action) {
    final MenuFactory headerMenuFactory = getHeaderMenuFactory();
    action.accept(this, headerMenuFactory);
    return this;
  }

}
