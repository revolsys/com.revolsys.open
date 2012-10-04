package com.revolsys.swing.list;

import javax.swing.AbstractListModel;

import com.revolsys.collection.ResultPager;

public class ResultPagerListModel<T> extends AbstractListModel {
  private static final long serialVersionUID = 1L;

  private ResultPager<T> pager;

  public ResultPagerListModel() {
  }

  public ResultPagerListModel(ResultPager<T> pager) {
    setPager(pager);
  }

  @Override
  public int getSize() {
    return pager.getNumResults();
  }

  @Override
  public Object getElementAt(int index) {
    pager.setPageNumber(index + 1);
    return pager.getList().get(0);
  }

  public ResultPager<T> getPager() {
    return pager;
  }

  public void setPager(ResultPager<T> pager) {
    this.pager = pager;
    pager.setPageSize(1);
  }

}
