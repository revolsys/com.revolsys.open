package com.revolsys.gis.algorithm.index.quadtree;

import java.util.ArrayList;
import java.util.List;

public class Node<T> extends AbstractNode<T> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final List<T> items = new ArrayList<>();

  private final List<double[]> boundingBoxes = new ArrayList<>();

  public Node() {
  }

  public Node(final int level, final double... bounds) {
    super(level, bounds);
  }

  @Override
  protected AbstractNode<T> createNode(final int level, final double... newBounds) {
    return new Node<T>(level, newBounds);
  }

  @Override
  protected void doAdd(final QuadTree<T> tree, final double[] bounds, final T item) {
    this.boundingBoxes.add(bounds);
    this.items.add(item);
  }

  @Override
  protected void doRemove(final int index) {
    this.boundingBoxes.remove(index);
    this.items.remove(index);
  }

  @Override
  protected double[] getBounds(final QuadTree<T> tree, final int i) {
    return this.boundingBoxes.get(i);
  }

  @Override
  protected T getItem(final QuadTree<T> tree, final int i) {
    return this.items.get(i);
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

}
