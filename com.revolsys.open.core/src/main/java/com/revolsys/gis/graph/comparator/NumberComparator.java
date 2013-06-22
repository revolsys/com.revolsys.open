package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

public class NumberComparator<T extends Number> implements Comparator<T> {

  public static int numberCompare(final Number o1, final Number o2) {
    return Double.compare(o1.doubleValue(), o2.doubleValue());
  }

  @Override
  public int compare(final T o1, final T o2) {
    return numberCompare(o1, o2);
  }

}
