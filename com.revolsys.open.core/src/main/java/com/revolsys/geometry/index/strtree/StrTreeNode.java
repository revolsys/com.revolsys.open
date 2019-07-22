package com.revolsys.geometry.index.strtree;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;

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
    final BoundingBoxEditor bounds = new BoundingBoxEditor();
    for (final Boundable childBoundable : getChildBoundables()) {
      bounds.addBbox((BoundingBox)childBoundable.getBounds());
    }
    if (bounds.isEmpty()) {
      return null;
    } else {
      return bounds.getBoundingBox();
    }
  }
}
