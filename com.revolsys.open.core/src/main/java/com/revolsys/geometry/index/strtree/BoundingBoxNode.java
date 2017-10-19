package com.revolsys.geometry.index.strtree;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class BoundingBoxNode<I> extends AbstractNode<BoundingBox, I> {
  private static final long serialVersionUID = 1L;

  public BoundingBoxNode(final int capacity, final int level) {
    super(capacity, level);
  }

  @Override
  protected BoundingBox computeBounds() {
    double x1 = Double.POSITIVE_INFINITY;
    double y1 = Double.POSITIVE_INFINITY;
    double x2 = Double.NEGATIVE_INFINITY;
    double y2 = Double.NEGATIVE_INFINITY;
    final int childCount = this.childCount;
    final Boundable<BoundingBox, I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<BoundingBox, I> child = children[i];
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
}
