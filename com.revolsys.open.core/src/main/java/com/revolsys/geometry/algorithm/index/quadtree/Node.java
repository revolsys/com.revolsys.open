package com.revolsys.geometry.algorithm.index.quadtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.util.BoundingBoxUtil;

public class Node<T> extends AbstractNode<T> {
  private static final long serialVersionUID = 1L;

  private final List<double[]> boundingBoxes = new ArrayList<>();

  private final List<T> items = new ArrayList<>();

  public Node() {
  }

  public Node(final int level, final double... bounds) {
    super(level, bounds);
  }

  @Override
  protected void addDo(final QuadTree<T> tree, final double[] bounds, final T item) {
    final List<T> items = this.items;
    synchronized (this.nodes) {
      this.boundingBoxes.add(bounds);
      items.add(item);
    }
  }

  @Override
  protected boolean changeItem(final QuadTree<T> tree, final double[] bounds, final T item) {
    final List<T> items = this.items;
    synchronized (this.nodes) {
      for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
        final T oldItem = items.get(itemIndex);
        if (tree.equalsItem(item, oldItem)) {
          items.set(itemIndex, item);
          this.boundingBoxes.set(itemIndex, bounds);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final Consumer<T> action) {
    final List<T> items = this.items;
    synchronized (this.nodes) {
      for (final T item : items) {
        action.accept(item);
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double[] bounds,
    final Consumer<T> action) {
    final List<T> items = this.items;
    synchronized (this.nodes) {
      int itemIndex = 0;
      for (final T item : items) {
        final double[] itemBounds = this.boundingBoxes.get(itemIndex);
        if (BoundingBoxUtil.intersects(bounds, itemBounds)) {
          action.accept(item);
        }
        itemIndex++;
      }
    }
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  @Override
  public boolean hasItems() {
    return !this.items.isEmpty();
  }

  @Override
  protected AbstractNode<T> newNode(final int level, final double... newBounds) {
    return new Node<>(level, newBounds);
  }

  @Override
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    final List<T> items = this.items;
    synchronized (this.nodes) {
      int itemIndex = 0;
      for (final Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
        final T existingItem = iterator.next();
        if (tree.equalsItem(item, existingItem)) {
          this.boundingBoxes.remove(itemIndex);
          iterator.remove();
          return true;
        }
        itemIndex++;
      }
      return false;
    }
  }
}
