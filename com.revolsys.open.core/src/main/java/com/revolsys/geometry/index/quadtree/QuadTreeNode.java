package com.revolsys.geometry.index.quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.util.BoundingBoxUtil;

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
  public void add(final QuadTree<T> tree, final double minX, final double minY, final double maxX,
    final double maxY, final T item) {
    final double[] bounds = new double[] {
      minX, minY, maxX, maxY
    };
    int i = 0;
    for (final T oldItem : this.items) {
      if (tree.equalsItem(item, oldItem)) {
        this.boundingBoxes.set(i, bounds);
        this.items.set(i, item);
        return;
      }
      i++;
    }
    this.boundingBoxes.add(bounds);
    this.items.add(item);
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final Consumer<? super T> action) {
    for (final T item : this.items) {
      action.accept(item);
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double[] bounds,
    final Consumer<? super T> action) {
    int i = 0;
    for (final double[] itemBounds : this.boundingBoxes) {
      if (BoundingBoxUtil.intersects(bounds, itemBounds)) {
        final T item = this.items.get(i);
        action.accept(item);
      }
      i++;
    }
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
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    int i = 0;
    for (final T oldItem : this.items) {
      if (tree.equalsItem(item, oldItem)) {
        this.items.remove(i);
        this.boundingBoxes.remove(i);
        return true;
      }
      i++;
    }
    return false;
  }

}
