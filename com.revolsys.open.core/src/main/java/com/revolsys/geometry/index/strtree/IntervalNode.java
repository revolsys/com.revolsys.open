package com.revolsys.geometry.index.strtree;

class IntervalNode<I> extends AbstractNode<Interval, I> {
  private static final long serialVersionUID = 1L;

  public IntervalNode(final int level) {
    super(level);
  }

  @Override
  protected Interval computeBounds() {
    Interval bounds = null;
    for (final Boundable<Interval, I> child : this) {
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
