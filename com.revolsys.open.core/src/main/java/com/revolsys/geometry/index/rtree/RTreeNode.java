package com.revolsys.geometry.index.rtree;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.BoundingBoxNode;
import com.revolsys.geometry.model.BoundingBox;

public abstract class RTreeNode<T> extends BoundingBoxNode {

  public RTreeNode() {
  }

  @Override
  protected void expandBoundingBox(final BoundingBox boundingBox) {
    super.expandBoundingBox(boundingBox);
  }

  public abstract void forEach(double minX, double minY, double maxX, double maxY,
    Consumer<? super T> action);

  public abstract void forEach(double minX, double minY, double maxX, double maxY,
    Predicate<? super T> filter, Consumer<? super T> action);

  public abstract void forEach(final double x, final double y, final Predicate<? super T> filter,
    final Consumer<? super T> action);

  public abstract void forEachValue(Consumer<? super T> action);

  public abstract void forEachValue(Predicate<? super T> filter, Consumer<? super T> action);

  public abstract boolean remove(LinkedList<RTreeNode<T>> path, final double minX,
    final double minY, final double maxX, final double maxY, T object);

  protected abstract void updateEnvelope();
}
