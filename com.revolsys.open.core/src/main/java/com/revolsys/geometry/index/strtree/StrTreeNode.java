package com.revolsys.geometry.index.strtree;

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
    return BoundingBox.bboxNew(getChildBoundables());
  }
}
