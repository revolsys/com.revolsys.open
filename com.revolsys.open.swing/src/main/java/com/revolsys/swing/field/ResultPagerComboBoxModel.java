package com.revolsys.swing.field;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import com.revolsys.collection.ResultPager;
import com.revolsys.swing.list.ResultPagerListCellRenderer;

@SuppressWarnings("serial")
public class ResultPagerComboBoxModel<T> extends AbstractListModel implements
  ComboBoxModel {

  public static <T> JComboBox create(final ResultPager<T> codeTable,
    boolean allowNull, String... attributeNames) {
    ResultPagerComboBoxModel<T> model = new ResultPagerComboBoxModel<T>(
      codeTable, allowNull);
    JComboBox comboBox = new JComboBox(model);
    ResultPagerListCellRenderer renderer = new ResultPagerListCellRenderer(
      attributeNames);
    comboBox.setRenderer(renderer);
    comboBox.setEditable(false);
    return comboBox;
  }

  public static final Object NULL = new Object();

  private Object selectedItem;

  private ResultPager<T> pager;

  private boolean allowNull;

  public ResultPagerComboBoxModel() {
    this(null, true);
  }

  public ResultPagerComboBoxModel(final ResultPager<T> pager) {
    this(pager, true);
  }

  public ResultPagerComboBoxModel(ResultPager<T> pager, boolean allowNull) {
    setPager(pager);
    this.allowNull = allowNull;
  }

  public void setPager(final ResultPager<T> pager) {
    if (this.pager != pager) {
      if (this.pager != null) {
        this.pager.close();
      }
      this.pager = pager;
      if (pager != null) {
        pager.setPageSize(1);
      }
    }
    fireContentsChanged(this, -1, -1);
  }

  @Override
  public Object getElementAt(int index) {
    if (allowNull) {
      if (index == 0) {
        return NULL;
      }
      index--;
    }
    if (index < getSize()) {
      pager.setPageNumber(index + 1);
      return pager.getList().get(0);
    } else {
      return null;
    }
  }

  @Override
  public Object getSelectedItem() {
    if (selectedItem == NULL) {
      return null;
    } else {
      return selectedItem;
    }
  }

  @Override
  public int getSize() {
    int size;
    if (pager == null) {
      size = 0;
    } else {
      size = pager.getNumResults();
    }
    if (allowNull) {
      size++;
    }
    return size;
  }

  @Override
  public void setSelectedItem(final Object item) {
    if ((selectedItem != null && !selectedItem.equals(item))
      || selectedItem == null && item != null) {
      selectedItem = item;
    }
  }

}
