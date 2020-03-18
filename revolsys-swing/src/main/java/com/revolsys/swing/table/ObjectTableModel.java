package com.revolsys.swing.table;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.util.Property;

public class ObjectTableModel<T> extends AbstractTableModel {
  /**
   *
   */
  private static final long serialVersionUID = -1243907349293763360L;

  private final String[] lables;

  private final String[] propertyNames;

  private final List<T> rows = new ArrayList<>();

  public ObjectTableModel(final String[] propertyNames, final String[] lables) {
    this.propertyNames = propertyNames;
    this.lables = lables;
  }

  public void addRow(final T row) {
    insertRow(getRowCount(), row);
  }

  @Override
  public int getColumnCount() {
    return this.propertyNames.length;
  }

  @Override
  public String getColumnName(final int column) {
    return this.lables[column];
  }

  @Override
  public int getRowCount() {
    return this.rows.size();
  }

  public List<T> getRows() {
    return this.rows;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Object row = this.rows.get(rowIndex);
    final String propertyName = this.propertyNames[columnIndex];
    return Property.getSimple(row, propertyName);
  }

  public void insertRow(final int rowIndex, final T row) {
    this.rows.add(rowIndex, row);
    fireTableRowsInserted(rowIndex, rowIndex);
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return true;
  }

  public void removeRow(final int rowIndex) {
    this.rows.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  public void setRows(final List<T> rows) {
    this.rows.clear();
    this.rows.addAll(rows);
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Object row = this.rows.get(rowIndex);
    final String propertyName = this.propertyNames[columnIndex];
    Property.setSimple(row, propertyName, value);
  }

}
