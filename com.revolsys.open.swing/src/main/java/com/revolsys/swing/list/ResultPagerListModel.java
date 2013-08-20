package com.revolsys.swing.list;

import javax.swing.AbstractListModel;

import com.revolsys.collection.ResultPager;

public class ResultPagerListModel<T> extends AbstractListModel {
  private static final long serialVersionUID = 1L;

  private ResultPager<T> pager;

  public ResultPagerListModel() {
  }

  public ResultPagerListModel(final ResultPager<T> pager) {
    setPager(pager);
  }

  @Override
  public Object getElementAt(final int index) {
    this.pager.setPageNumber(index + 1);
    return this.pager.getList().get(0);
  }

  public ResultPager<T> getPager() {
    return this.pager;
  }

  @Override
  public int getSize() {
    return this.pager.getNumResults();
  }

  public void setPager(final ResultPager<T> pager) {
    this.pager = pager;
    pager.setPageSize(1);
  }

}
