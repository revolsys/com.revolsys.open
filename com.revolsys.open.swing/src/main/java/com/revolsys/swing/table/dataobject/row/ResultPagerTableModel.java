package com.revolsys.swing.table.dataobject.row;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.revolsys.collection.ResultPager;
import com.revolsys.util.JavaBeanUtil;

public class ResultPagerTableModel<T> extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private List<String> attributeNames = new ArrayList<String>();

  private List<String> titles = new ArrayList<String>();

  private ResultPager<T> pager;

  public ResultPagerTableModel() {
  }

  public ResultPagerTableModel(final List<String> attributeNames) {
    this(attributeNames, attributeNames, null);
  }

  public ResultPagerTableModel(final List<String> attributeNames,
    final List<String> titles) {
    this(attributeNames, titles, null);
  }

  public ResultPagerTableModel(final List<String> attributeNames,
    final List<String> titles, final ResultPager<T> pager) {
    this.attributeNames = new ArrayList<String>(attributeNames);
    this.titles = new ArrayList<String>(titles);
    setPager(pager);
  }

  public ResultPagerTableModel(final List<String> attributeNames,
    final ResultPager<T> pager) {
    this(attributeNames, attributeNames, pager);
  }

  public void dispose() {
    titles = null;
    attributeNames = null;
    pager = null;
  }

  @Override
  public int getColumnCount() {
    return attributeNames.size();
  }

  @Override
  public String getColumnName(final int column) {
    return titles.get(column);
  }

  public T getObject(final int index) {
    if (pager == null) {
      return null;
    } else {
      pager.setPageNumber(index + 1);
      return pager.getList().get(0);
    }
  }

  public List<T> getObjects(final int[] rows) {
    final List<T> objects = new ArrayList<T>();
    for (final int row : rows) {
      final T object = getObject(row);
      objects.add(object);
    }
    return objects;
  }

  /**
   * @return the pager
   */
  public ResultPager<T> getPager() {
    return pager;
  }

  @Override
  public int getRowCount() {
    if (pager == null) {
      return 0;
    } else {
      return pager.getNumResults();
    }
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final T object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      final String columnName = attributeNames.get(columnIndex);
      return JavaBeanUtil.getValue(object, columnName);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  /**
   * @param pager the pager to set
   */
  public void setPager(final ResultPager<T> pager) {
    if (this.pager != pager) {
      if (this.pager != null) {
        this.pager.close();
      }
      this.pager = pager;
      if (pager != null) {
        pager.setPageSize(1);
      }
      fireTableDataChanged();
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    throw new UnsupportedOperationException(
      "Editing is not currently supoorted");
  }

}
