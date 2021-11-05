package com.revolsys.collection.list;

import java.util.List;

import org.jeometry.common.collection.list.AbstractDelegatingList;

public class DelegatingList<V> extends AbstractDelegatingList<V> {

  private final List<V> list;

  public DelegatingList(final List<V> list) {
    super(true);
    this.list = list;
  }

  @Override
  public List<V> getList() {
    return this.list;
  }

}
