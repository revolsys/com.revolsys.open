package com.revolsys.collection.set;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntRangeIterator implements Iterator<Integer> {
  private int current;

  private int to;

  private boolean hasNext = true;

  public IntRangeIterator(final int from, final int to) {
    if (from < to) {
      this.current = from;
      this.to = to;
    } else {
      this.current = to;
      this.to = from;
    }
  }

  @Override
  public boolean hasNext() {
    return this.hasNext;
  }

  @Override
  public Integer next() {
    if (this.hasNext) {
      final int next = this.current;
      if (this.current == Integer.MAX_VALUE) {
        this.hasNext = false;
      } else {
        this.current++;
        if (this.current > this.to) {
          this.hasNext = false;
        }
      }
      return next;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
