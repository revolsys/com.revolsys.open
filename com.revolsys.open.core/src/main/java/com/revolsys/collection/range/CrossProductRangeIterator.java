package com.revolsys.collection.range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.util.CollectionUtil;

public class CrossProductRangeIterator extends AbstractIterator<String> {
  private final List<String> currentValues = new ArrayList<>();

  private final CrossProductRange range;

  private List<Iterator<? extends Object>> iterators = new ArrayList<>();

  public CrossProductRangeIterator(final CrossProductRange range) {
    this.range = range;
    for (final AbstractRange<?> subRange : range.getRanges()) {
      final Iterator<?> iterator = subRange.iterator();
      if (iterator.hasNext()) {
        final Object value = iterator.next();
        this.currentValues.add(value.toString());
        this.iterators.add(iterator);
      } else {
        this.iterators.add(null);
        this.currentValues.add(null);
      }
    }
    if (this.iterators.isEmpty()) {
      this.iterators = null;
    }
  }

  @Override
  protected String getNext() throws NoSuchElementException {
    if (this.iterators == null) {
      throw new NoSuchElementException();
    } else {
      final String value = CollectionUtil.toString("", this.currentValues);
      updateValues();
      return value;
    }
  }

  private void updateValues() {
    for (int i = this.iterators.size() - 1; i > -1; i--) {
      Iterator<? extends Object> iterator = this.iterators.get(i);
      if (iterator == null) {
      } else if (iterator.hasNext()) {
        final Object value = iterator.next();
        this.currentValues.set(i, value.toString());
        return;
      } else {
        iterator = this.range.getRange(i).iterator();
        this.iterators.set(i, iterator);
        String value = null;
        if (iterator.hasNext()) {
          value = iterator.next().toString();
        } else {
          iterator = null;
        }
        this.currentValues.set(i, value);
        this.iterators.set(i, iterator);
      }
    }
    this.iterators = null;
  }

}
