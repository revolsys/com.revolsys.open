package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;

public abstract class RTreeNode<T> {

  private BoundingBox boundingBox;

  public RTreeNode() {
  }

  public boolean contains(final BoundingBox boundingBox) {
    return boundingBox.covers(boundingBox);
  }

  public double getArea() {
    return this.boundingBox.getArea();
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public abstract boolean remove(LinkedList<RTreeNode<T>> path,
    BoundingBox envelope, T object);

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public String toString() {
    return this.boundingBox.toString();
  }

  protected abstract void updateEnvelope();

  public abstract boolean visit(BoundingBox envelope, Filter<T> filter,
    Visitor<T> visitor);

  public abstract boolean visit(BoundingBox envelope, Visitor<T> visitor);

  public abstract boolean visit(Visitor<T> visitor);
}
