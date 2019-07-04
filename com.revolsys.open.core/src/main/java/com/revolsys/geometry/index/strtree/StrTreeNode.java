package com.revolsys.geometry.index.strtree;

import java.util.Iterator;

import com.revolsys.geometry.model.BoundingBox;

public final class StrTreeNode<I> extends AbstractNode {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public StrTreeNode(final int level) {
    super(level);
  }

  @Override
  protected Object computeBounds() {
    BoundingBox bounds = null;
    for (final Iterator i = getChildBoundables().iterator(); i.hasNext();) {
      final Boundable childBoundable = (Boundable)i.next();
      if (bounds == null) {
        bounds = (BoundingBox)childBoundable.getBounds();
      } else {
        bounds = bounds.expandToInclude((BoundingBox)childBoundable.getBounds());
      }
    }
    return bounds;
  }
}
