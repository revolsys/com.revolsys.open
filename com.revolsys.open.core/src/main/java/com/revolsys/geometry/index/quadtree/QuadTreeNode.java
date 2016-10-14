package com.revolsys.geometry.index.quadtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuadTreeNode<T> extends AbstractNode<T> {
  private static final long serialVersionUID = 1L;

  private final List<double[]> boundingBoxes = new ArrayList<>();

  private final List<T> items = new ArrayList<>();

  public QuadTreeNode() {
  }

  public QuadTreeNode(final int level, final double minX, final double minY, final double maxX,
    final double maxY) {
    super(level, minX, minY, maxX, maxY);
  }

  @Override
  protected void addDo(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    this.boundingBoxes.add(new double[] {
      minX, minY, maxX, maxY
    });
    this.items.add(item);
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

  @Override
  protected AbstractNode<T> newNode(final int level, final double minX, final double minY,
    final double maxX, final double maxY) {
    return new QuadTreeNode<>(level, minX, minY, maxX, maxY);
  }

  @Override
  protected void removeDo(final int index) {
    this.boundingBoxes.remove(index);
    this.items.remove(index);
  }

  @Override
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    boolean removed = false;
    int index = 0;
    for (final Iterator<T> iterator = this.items.iterator(); iterator.hasNext();) {
      final T item2 = iterator.next();
      if (tree.equalsItem(item, item2)) {
        this.boundingBoxes.remove(index);
        iterator.remove();
        removed = true;
      }
      index++;
    }
    return removed;
  }

}
