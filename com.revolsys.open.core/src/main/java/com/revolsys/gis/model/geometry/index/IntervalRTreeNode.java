package com.revolsys.gis.model.geometry.index;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.io.WKTWriter;

public abstract class IntervalRTreeNode<V> {

  private double min = Double.POSITIVE_INFINITY;

  private double max = Double.NEGATIVE_INFINITY;

  public IntervalRTreeNode() {
  }

  public IntervalRTreeNode(final double min, final double max) {
    this.setMin(min);
    this.setMax(max);
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

  protected void setMax(final double max) {
    this.max = max;
  }

  protected void setMin(final double min) {
    this.min = min;
  }

  @Override
  public String toString() {
    return WKTWriter.toLineString(new Coordinate(getMin(), 0), new Coordinate(
      getMax(), 0));
  }
}
