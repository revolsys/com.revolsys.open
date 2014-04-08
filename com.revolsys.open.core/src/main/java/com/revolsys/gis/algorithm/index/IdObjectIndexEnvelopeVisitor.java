package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.Envelope;

public final class IdObjectIndexEnvelopeVisitor<T> implements Visitor<Integer> {
  private final IdObjectIndex<T> index;

  private final Envelope envelope;

  private final Visitor<T> visitor;

  public IdObjectIndexEnvelopeVisitor(final IdObjectIndex<T> index,
    final Envelope envelope, final Visitor<T> visitor) {
    this.index = index;
    this.envelope = envelope;
    this.visitor = visitor;
  }

  @Override
  public boolean visit(final Integer id) {
    final T object = index.getObject(id);
    final Envelope e = index.getEnvelope(object);
    if (e.intersects(envelope)) {
      if (!visitor.visit(object)) {
        return false;
      }
    }
    return true;
  }
}
