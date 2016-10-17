package com.revolsys.geometry.index.quadtree;

import java.util.function.Consumer;

public class IdObjectNode<T> extends AbstractQuadTreeNode<T> {
  private static final long serialVersionUID = 1L;

  private Object[] ids = new Object[0];

  public IdObjectNode() {
  }

  public IdObjectNode(final int level, final double minX, final double minY, final double maxX,
    final double maxY) {
    super(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean add(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    final Object id = idObjectTree.getId(item);
    int i = 0;
    for (final Object oldId : this.ids) {
      if (oldId.equals(id)) {
        this.ids[i] = item;
        return false;
      }
      i++;
    }
    final int length = this.ids.length;
    final Object[] newIds = new Object[length + 1];
    System.arraycopy(this.ids, 0, newIds, 0, length);
    newIds[length] = id;
    this.ids = newIds;
    return true;
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final Consumer<? super T> action) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    for (final Object id : this.ids) {
      final T item = idObjectTree.getItem(id);
      action.accept(item);
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double x, final double y,
    final Consumer<? super T> action) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    for (final Object id : this.ids) {
      if (idObjectTree.intersectsBounds(id, x, y)) {
        final T item = idObjectTree.getItem(id);
        action.accept(item);
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<? super T> action) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    for (final Object id : this.ids) {
      if (idObjectTree.intersectsBounds(id, minX, minY, maxX, maxY)) {
        final T item = idObjectTree.getItem(id);
        action.accept(item);
      }
    }
  }

  @Override
  public int getItemCount() {
    return this.ids.length;
  }

  @Override
  protected AbstractQuadTreeNode<T> newNode(final int level, final double minX, final double minY,
    final double maxX, final double maxY) {
    return new IdObjectNode<>(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    final Object id = idObjectTree.getId(item);
    int index = 0;
    for (final Object oldId : this.ids) {
      if (id.equals(oldId)) {
        final int length = this.ids.length;
        final int newLength = length - 1;
        if (newLength == 0) {
          this.ids = new Object[0];
        } else {
          final Object[] newIds = new Object[newLength];
          if (index > 0) {
            System.arraycopy(this.ids, 0, newIds, 0, index);
          }
          if (index < newLength) {
            System.arraycopy(this.ids, index + 1, newIds, index, length - index - 1);
          }
          this.ids = newIds;
        }
        return true;
      }
      index++;
    }
    return false;
  }
}
