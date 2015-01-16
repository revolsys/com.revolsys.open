package com.revolsys.collection.bplus;

import java.util.AbstractSet;
import java.util.Iterator;

class BPlusTreeLeafSet<T> extends AbstractSet<T> {

  private final BPlusTreeMap<?, ?> map;

  private final boolean key;

  public BPlusTreeLeafSet(final BPlusTreeMap<?, ?> map, final boolean key) {
    this.map = map;
    this.key = key;
  }

  @Override
  public Iterator<T> iterator() {
    return new BPlusTreeLeafIterator<T>(this.map, this.key);
  }

  @Override
  public int size() {
    return this.map.size();
  }

}
