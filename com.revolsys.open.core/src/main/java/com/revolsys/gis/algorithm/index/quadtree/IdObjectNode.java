package com.revolsys.gis.algorithm.index.quadtree;

import java.util.ArrayList;
import java.util.List;

public class IdObjectNode<T> extends AbstractNode<T> {

  private final List<Object> ids = new ArrayList<>();

  public IdObjectNode(final int level, final double... bounds) {
    super(level, bounds);
  }

  @Override
  protected AbstractNode<T> createNode(final int level,
    final double... newBounds) {
    return new IdObjectNode<T>(level, newBounds);
  }

  @Override
  protected void doAdd(final QuadTree<T> tree, final double[] bounds,
    final T item) {
    final Object id = ((IdObjectQuadTree<T>)tree).getId(item);
    ids.add(id);
  }

  @Override
  protected void doRemove(final int index) {
    ids.remove(index);
  }

  @Override
  protected double[] getBounds(final QuadTree<T> tree, final int i) {
    final Object id = ids.get(i);
    return ((IdObjectQuadTree<T>)tree).getBounds(id);
  }

  @Override
  protected T getItem(final QuadTree<T> tree, final int i) {
    final Object id = ids.get(i);
    return ((IdObjectQuadTree<T>)tree).getItem(id);
  }

  @Override
  public int getItemCount() {
    return ids.size();
  }

}
