package com.revolsys.gis.model.geometry.index;

import java.util.Comparator;

public class NodeComparator<V> implements Comparator<IntervalRTreeNode<V>> {
  @Override
  public int compare(final IntervalRTreeNode<V> n1,
    final IntervalRTreeNode<V> n2) {
    final double mid1 = (n1.min + n1.max) / 2;
    final double mid2 = (n2.min + n2.max) / 2;
    if (mid1 < mid2) {
      return -1;
    }
    if (mid1 > mid2) {
      return 1;
    }
    return 0;
  }
}
