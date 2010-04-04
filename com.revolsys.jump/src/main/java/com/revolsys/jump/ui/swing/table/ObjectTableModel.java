package com.revolsys.jump.ui.swing.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.revolsys.jump.util.BeanUtil;

public class ObjectTableModel<T> extends AbstractTableModel {
  /**
   * 
   */
  private static final long serialVersionUID = -1243907349293763360L;

  private String[] propertyNames;

  private String[] lables;

  private List<T> rows = new ArrayList<T>();

  public ObjectTableModel(final String[] propertyNames, final String[] lables) {
    this.propertyNames = propertyNames;
    this.lables = lables;
  }

  public int getColumnCount() {
    return propertyNames.length;
  }

  public String getColumnName(final int column) {
    return lables[column];
  }

  public int getRowCount() {
    return rows.size();
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    Object row = rows.get(rowIndex);
    String propertyName = propertyNames[columnIndex];
    return BeanUtil.getProperty(row, propertyName);
  }

  public boolean isCellEditable(final int row, final int column) {
    return true;
  }

  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    Object row = rows.get(rowIndex);
    String propertyName = propertyNames[columnIndex];
    BeanUtil.setProperty(row, propertyName, value);
  }

  public void addRow(final T row) {
    insertRow(getRowCount(), row);
  }

  public void insertRow(final int rowIndex, final T row) {
    rows.add(rowIndex, row);
    fireTableRowsInserted(rowIndex, rowIndex);
  }

  public void removeRow(final int rowIndex) {
    rows.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  public List<T> getRows() {
    return rows;
  }

  public void setRows(final List<T> rows) {
    this.rows.clear();
    this.rows.addAll(rows);
    fireTableDataChanged();
  }

}
