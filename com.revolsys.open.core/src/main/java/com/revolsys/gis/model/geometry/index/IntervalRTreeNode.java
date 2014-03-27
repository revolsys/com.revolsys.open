package com.revolsys.gis.model.geometry.index;

import com.revolsys.collection.Visitor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;

public abstract class IntervalRTreeNode<V> {

  protected double min = Double.POSITIVE_INFINITY;

  protected double max = Double.NEGATIVE_INFINITY;

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
    if (min > queryMax || max < queryMin) {
      return false;
    }
    return true;
  }

  public abstract void query(double queryMin, double queryMax,
    Visitor<V> visitor);

  @Override
  public String toString() {
    return WKTWriter.toLineString(new Coordinate(min, 0),
      new Coordinate(max, 0));
  }

}
