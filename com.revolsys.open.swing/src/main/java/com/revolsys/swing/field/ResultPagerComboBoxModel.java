package com.revolsys.swing.field;

import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.revolsys.collection.LruMap;
import com.revolsys.collection.ResultPager;
import com.revolsys.swing.list.ResultPagerListCellRenderer;

@SuppressWarnings("serial")
public class ResultPagerComboBoxModel<T> extends AbstractListModel implements
  ComboBoxModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final Object NULL = new Object();

  public static <T> ComboBox create(final ResultPager<T> codeTable,
    final boolean allowNull, final String... attributeNames) {
    final ResultPagerComboBoxModel<T> model = new ResultPagerComboBoxModel<T>(
      codeTable, allowNull);
    final ComboBox comboBox = new ComboBox(model);
    final ResultPagerListCellRenderer renderer = new ResultPagerListCellRenderer(
      attributeNames);
    comboBox.setRenderer(renderer);
    comboBox.setEditable(false);
    return comboBox;
  }

  private final Map<Integer, T> cache = new LruMap<Integer, T>(200);

  private Object selectedItem;

  private ResultPager<T> pager;

  private boolean allowNull;

  public ResultPagerComboBoxModel() {
    this(null, true);
  }

  public ResultPagerComboBoxModel(final ResultPager<T> pager) {
    this(pager, true);
  }

  public ResultPagerComboBoxModel(final ResultPager<T> pager,
    final boolean allowNull) {
    setPager(pager);
    this.allowNull = allowNull;
  }

  @Override
  public Object getElementAt(int index) {
    if (this.allowNull) {
      if (index == 0) {
        return NULL;
      }
      index--;
    }
    if (index < getSize()) {
      synchronized (this.cache) {
        // TODO load in background
        T value = this.cache.get(index);
        if (value == null) {
          this.pager.setPageNumber((int)Math.floor(index / 100.0) + 1);
          final List<T> values = this.pager.getList();
          int i = index;
          for (final T result : values) {
            this.cache.put(i, result);
            i++;
          }
          value = this.cache.get(index);
        }
        return value;
      }
    }
    return null;
  }

  @Override
  public Object getSelectedItem() {
    if (this.selectedItem == NULL) {
      return null;
    } else {
      return this.selectedItem;
    }
  }

  @Override
  public int getSize() {
    int size;
    if (this.pager == null) {
      size = 0;
    } else {
      size = this.pager.getNumResults();
    }
    if (this.allowNull) {
      size++;
    }
    return size;
  }

  public void setPager(final ResultPager<T> pager) {
    if (this.pager != pager) {
      if (this.pager != null) {
        this.pager.close();
      }
      this.cache.clear();
      this.pager = pager;
      if (pager != null) {
        pager.setPageSize(100);
      }
    }
    fireContentsChanged(this, -1, -1);
  }

  @Override
  public void setSelectedItem(final Object item) {
    if (this.selectedItem != null && !this.selectedItem.equals(item)
      || this.selectedItem == null && item != null) {
      this.selectedItem = item;
    }
  }

}
