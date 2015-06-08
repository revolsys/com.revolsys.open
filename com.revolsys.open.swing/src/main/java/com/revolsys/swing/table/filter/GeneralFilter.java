package com.revolsys.swing.table.filter;

import javax.swing.RowFilter;

public abstract class GeneralFilter extends RowFilter<Object, Object> {
  protected static void checkIndices(final int[] columns) {
    for (int i = columns.length - 1; i >= 0; i--) {
      if (columns[i] < 0) {
        throw new IllegalArgumentException("Index must be >= 0");
      }
    }
  }

  private final int[] columns;

  public GeneralFilter(final int[] columns) {
    checkIndices(columns);
    this.columns = columns;
  }

  @Override
  public boolean include(final Entry<? extends Object, ? extends Object> value) {
    int count = value.getValueCount();
    if (this.columns.length > 0) {
      for (int i = this.columns.length - 1; i >= 0; i--) {
        final int index = this.columns[i];
        if (index < count) {
          if (include(value, index)) {
            return true;
          }
        }
      }
    } else {
      while (--count >= 0) {
        if (include(value, count)) {
          return true;
        }
      }
    }
    return false;
  }

  protected abstract boolean include(Entry<? extends Object, ? extends Object> value, int index);
}
