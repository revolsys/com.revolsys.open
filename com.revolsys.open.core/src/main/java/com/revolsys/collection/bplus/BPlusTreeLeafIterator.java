package com.revolsys.collection.bplus;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;

class BPlusTreeLeafIterator<T> extends AbstractIterator<T> {

  private BPlusTreeMap<?, ?> map;

  private int modCount;

  private boolean key;

  private List<T> currentValues = new ArrayList<T>();

  private int currentIndex = 0;

  private int nextPageId = 0;

  public BPlusTreeLeafIterator(BPlusTreeMap<?, ?> map, boolean key) {
    this.map = map;
    this.key = key;
    modCount = map.getModCount();
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    if (map.getModCount() == modCount) {
      while (currentValues.isEmpty() || currentIndex >= currentValues.size()) {
        if (nextPageId < 0) {
          throw new NoSuchElementException();
        } else {
          nextPageId = map.getLeafValues(currentValues, nextPageId, key);
        }
      }
      T value = currentValues.get(currentIndex++);
      return value;
    } else {
      throw new ConcurrentModificationException();
    }
  }
}
