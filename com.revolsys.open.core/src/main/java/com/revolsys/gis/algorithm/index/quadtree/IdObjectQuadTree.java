package com.revolsys.gis.algorithm.index.quadtree;

public abstract class IdObjectQuadTree<T> extends QuadTree<T> {

  public IdObjectQuadTree() {
    super(new IdObjectNode<T>());
  }

  protected abstract double[] getBounds(Object id);

  protected abstract Object getId(T item);

  protected abstract T getItem(Object id);
}
