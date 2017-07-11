package com.revolsys.geometry.index.strtree;

import com.revolsys.geometry.model.BoundingBox;

public class BoundingBoxNode<I> extends AbstractNode<BoundingBox, I> {
  private static final long serialVersionUID = 1L;

  public BoundingBoxNode(final int level) {
    super(level);
  }

  @Override
  protected BoundingBox computeBounds() {
    BoundingBox bounds = null;
    for (final Boundable<BoundingBox, I> child : this) {
      final BoundingBox childBounds = child.getBounds();
      bounds = childBounds.expandToInclude(bounds);
    }
    return bounds;
  }
}
