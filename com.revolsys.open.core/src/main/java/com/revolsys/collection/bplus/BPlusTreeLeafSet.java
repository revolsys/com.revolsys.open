package com.revolsys.collection.bplus;

import java.util.AbstractSet;
import java.util.Iterator;

class BPlusTreeLeafSet<T> extends AbstractSet<T> {

  private BPlusTreeMap<?, ?> map;

  private boolean key;

  public BPlusTreeLeafSet(BPlusTreeMap<?, ?> map, boolean key) {
    this.map = map;
    this.key = key;
  }

  @Override
  public Iterator<T> iterator() {
    return new BPlusTreeLeafIterator<T>(map, key);
  }

  @Override
  public int size() {
    return map.size();
  }

}
