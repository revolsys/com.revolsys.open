package com.revolsys.gis.algorithm.index.quadtree;

import java.util.ArrayList;
import java.util.List;

public class Node<T> extends AbstractNode<T> {

  private final List<T> items = new ArrayList<>();

  private final List<double[]> boundingBoxes = new ArrayList<>();

  public Node(final int level, final double... bounds) {
    super(level, bounds);
  }

  @Override
  protected AbstractNode<T> createNode(final int level,
    final double... newBounds) {
    return new Node<T>(level, newBounds);
  }

  @Override
  protected void doAdd(final QuadTree<T> tree, final double[] bounds,
    final T item) {
    boundingBoxes.add(bounds);
    items.add(item);
  }

  @Override
  protected void doRemove(final int index) {
    boundingBoxes.remove(index);
    items.remove(index);
  }

  @Override
  protected double[] getBounds(final QuadTree<T> tree, final int i) {
    return boundingBoxes.get(i);
  }

  @Override
  protected T getItem(final QuadTree<T> tree, final int i) {
    return items.get(i);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

}
