package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

public class NumberComparator<T extends Number> implements Comparator<T> {

  @Override
  public int compare(final T o1, final T o2) {
    // TODO Auto-generated method stub
    return Double.compare(o1.doubleValue(), o2.doubleValue());
  }

}
