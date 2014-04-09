package com.revolsys.gis.model.geometry.index;

import com.revolsys.collection.Visitor;

public abstract class IntervalRTreeNode<V> {

  private double min = Double.POSITIVE_INFINITY;

  private double max = Double.NEGATIVE_INFINITY;

  public IntervalRTreeNode() {
  }

  public IntervalRTreeNode(final double min, final double max) {
    this.min = min;
    this.max = max;
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  protected boolean intersects(final double queryMin, final double queryMax) {
    if (getMin() > queryMax || getMax() < queryMin) {
      return false;
    }
    return true;
  }

  public abstract void query(double queryMin, double queryMax,
    Visitor<V> visitor);

  @Override
  public String toString() {
    final double min = getMin();
    final double max = getMax();
    return "LINESTRING(0 " + min + ",0 " + max + ")";
  }
}
