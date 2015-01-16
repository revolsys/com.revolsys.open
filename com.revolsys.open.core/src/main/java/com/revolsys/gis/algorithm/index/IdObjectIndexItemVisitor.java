package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.AbstractItemVisitor;
import com.revolsys.jts.geom.BoundingBox;

public final class IdObjectIndexItemVisitor<T> extends
AbstractItemVisitor<Integer> {
  private final IdObjectIndex<T> index;

  private final BoundingBox envelope;

  private final Visitor<T> visitor;

  public IdObjectIndexItemVisitor(final IdObjectIndex<T> index,
    final BoundingBox envelope, final Visitor<T> visitor) {
    this.index = index;
    this.envelope = envelope;
    this.visitor = visitor;
  }

  @Override
  public boolean visit(final Integer id) {
    final T object = this.index.getObject(id);
    final BoundingBox e = this.index.getEnvelope(object);
    if (e.intersects(this.envelope)) {
      this.visitor.visit(object);
    }
    return true;
  }
}
