package com.revolsys.gis.model.geometry.index;

import com.revolsys.collection.Visitor;

public class IntervalRTreeLeafNode<V> extends IntervalRTreeNode<V> {
  private final V item;

  public IntervalRTreeLeafNode(final double min, final double max, final V item) {
    super(min, max);
    this.item = item;
  }

  @Override
  public void query(final double queryMin, final double queryMax,
    final Visitor<V> visitor) {
    if (intersects(queryMin, queryMax)) {
      visitor.visit(item);
    }
  }

}
