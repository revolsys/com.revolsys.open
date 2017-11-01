package com.revolsys.geometry.index.strtree;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.util.Emptyable;

public class BoundingBoxNode<I> implements Emptyable, Boundable<I>, Serializable {
  private static final long serialVersionUID = 1L;

  private BoundingBox bounds = null;

  protected Boundable<I>[] children;

  protected int childCount;

  private final int level;

  /**
   * Constructs an BoundingBoxNode at the given level in the tree
   * @param level 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  @SuppressWarnings("unchecked")
  public BoundingBoxNode(final int nodeCapacity, final int level) {
    this.children = new Boundable[nodeCapacity];
    this.level = level;
  }

  /**
   * Adds either an AbstractNode, or if this is a leaf node, a data object
   * (wrapped in an ItemBoundable)
   */
  public void addChild(final Boundable<I> child) {
    this.children[this.childCount++] = child;
  }

  /**
   * @param level -1 to get items
   */
  @Override
  public void boundablesAtLevel(final int level, final Collection<Boundable<I>> boundables) {
    if (getLevel() == level) {
      boundables.add(this);
    } else {
      final int childCount = this.childCount;
      final Boundable<I>[] children = this.children;
      for (int i = 0; i < childCount; i++) {
        final Boundable<I> child = children[i];
        child.boundablesAtLevel(level, boundables);
      }
    }
  }

  protected BoundingBox computeBounds() {
    double x1 = Double.POSITIVE_INFINITY;
    double y1 = Double.POSITIVE_INFINITY;
    double x2 = Double.NEGATIVE_INFINITY;
    double y2 = Double.NEGATIVE_INFINITY;
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      final BoundingBox childBounds = child.getBounds();
      final double minX = childBounds.getMinX();
      if (minX < x1) {
        x1 = minX;
      }
      final double minY = childBounds.getMinY();
      if (minY < y1) {
        y1 = minY;
      }
      final double maxX = childBounds.getMaxX();
      if (maxX > x2) {
        x2 = maxX;
      }
      final double maxY = childBounds.getMaxY();
      if (maxY > y2) {
        y2 = maxY;
      }

    }
    return new BoundingBoxDoubleXY(x1, y1, x2, y2);
  }

  /**
   * Gets the bounds of this node
   *
   * @return the object representing bounds in this index
   */
  @Override
  public BoundingBox getBounds() {
    if (this.bounds == null) {
      this.bounds = computeBounds();
    }
    return this.bounds;
  }

  /**
   * Gets the count of the {@link Boundable}s at this node.
   *
   * @return the count of boundables at this node
   */
  @Override
  public int getChildCount() {
    return this.childCount;
  }

  @Override
  public Boundable<I>[] getChildren() {
    return this.children;
  }

  @Override
  public int getDepth() {
    int maxChildDepth = 0;
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      final int childDepth = child.getDepth();
      if (childDepth > maxChildDepth) {
        maxChildDepth = childDepth;
      }
    }
    return maxChildDepth + 1;
  }

  @Override
  public int getItemCount() {
    int itemCount = 0;
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      itemCount += child.getItemCount();
    }
    return itemCount;
  }

  /**
   * Returns 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * Tests whether there are any {@link Boundable}s at this node.
   *
   * @return true if there are boundables at this node
   */
  @Override
  public boolean isEmpty() {
    return this.childCount == 0;
  }

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public void query(final BoundingBox searchBounds, final Consumer<? super I> action) {
    final BoundingBox bounds = getBounds();
    final boolean intersectsBounds = bounds.intersectsBounds(searchBounds);
    if (intersectsBounds) {
      final int childCount = this.childCount;
      final Boundable<I>[] children = this.children;
      for (int i = 0; i < childCount; i++) {
        final Boundable<I> child = children[i];
        child.query(searchBounds, action);
      }
    }
  }
}
