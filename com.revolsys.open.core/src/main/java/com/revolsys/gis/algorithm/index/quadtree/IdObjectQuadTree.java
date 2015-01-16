package com.revolsys.gis.algorithm.index.quadtree;

public abstract class IdObjectQuadTree<T> extends QuadTree<T> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public IdObjectQuadTree() {
    super(new IdObjectNode<T>());
  }

  protected abstract double[] getBounds(Object id);

  protected abstract Object getId(T item);

  protected abstract T getItem(Object id);
}
