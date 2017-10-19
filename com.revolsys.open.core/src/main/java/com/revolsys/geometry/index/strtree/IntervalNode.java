package com.revolsys.geometry.index.strtree;

class IntervalNode<I> extends AbstractNode<Interval, I> {
  private static final long serialVersionUID = 1L;

  public IntervalNode(final int capacity, final int level) {
    super(capacity, level);
  }

  @Override
  protected Interval computeBounds() {
    Interval bounds = null;
    final int childCount = this.childCount;
    final Boundable<Interval, I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<Interval, I> child = children[i];
      final Interval childBounds = child.getBounds();
      if (bounds == null) {
        bounds = new Interval(childBounds);
      } else {
        bounds.expandToInclude(childBounds);
      }
    }
    return bounds;
  }
}
