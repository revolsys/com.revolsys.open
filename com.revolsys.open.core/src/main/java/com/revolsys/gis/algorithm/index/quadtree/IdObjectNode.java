package com.revolsys.gis.algorithm.index.quadtree;

public class IdObjectNode<T> extends AbstractNode<T> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Object[] ids;

  public IdObjectNode() {
  }

  public IdObjectNode(final int level, final double... bounds) {
    super(level, bounds);
  }

  @Override
  protected AbstractNode<T> createNode(final int level, final double... newBounds) {
    return new IdObjectNode<T>(level, newBounds);
  }

  @Override
  protected void doAdd(final QuadTree<T> tree, final double[] bounds, final T item) {
    final Object id = ((IdObjectQuadTree<T>)tree).getId(item);
    if (this.ids == null) {
      this.ids = new Object[] {
        id
      };
    } else {
      final int length = this.ids.length;
      final Object[] newIds = new Object[length + 1];
      System.arraycopy(this.ids, 0, newIds, 0, length);
      newIds[length] = id;
      this.ids = newIds;
    }
  }

  @Override
  protected void doRemove(final int index) {
    final int length = this.ids.length;
    final int newLength = length - 1;
    if (newLength == 0) {
      this.ids = null;
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
  }

  @Override
  protected double[] getBounds(final QuadTree<T> tree, final int i) {
    if (this.ids == null) {
      return null;
    } else {
      final Object id = this.ids[i];
      return ((IdObjectQuadTree<T>)tree).getBounds(id);
    }
  }

  @Override
  protected T getItem(final QuadTree<T> tree, final int i) {
    if (this.ids == null) {
      return null;
    } else {
      final Object id = this.ids[i];
      return ((IdObjectQuadTree<T>)tree).getItem(id);
    }
  }

  @Override
  public int getItemCount() {
    if (this.ids == null) {
      return 0;
    } else {
      return this.ids.length;
    }
  }

}
