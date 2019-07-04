package com.revolsys.geometry.algorithm.index;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;

public abstract class RTreeNode<T> {

  private BoundingBox boundingBox;

  public RTreeNode() {
  }

  public boolean contains(final BoundingBox boundingBox) {
    return boundingBox.bboxCovers(boundingBox);
  }

  public abstract void forEach(BoundingBox envelope, Consumer<T> action);

  public abstract void forEach(BoundingBox envelope, Predicate<T> filter, Consumer<T> action);

  public abstract void forEachValue(Consumer<? super T> action);

  public double getArea() {
    return this.boundingBox.getArea();
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public abstract boolean remove(LinkedList<RTreeNode<T>> path, BoundingBox envelope, T object);

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public String toString() {
    return this.boundingBox.toString();
  }

  protected abstract void updateEnvelope();
}
