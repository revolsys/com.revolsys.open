package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.revolsys.data.record.Record;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.index.ItemVisitor;

/**
 * A {@link ItemVisitor} implementation which uses a {@link Visitor} to visit
 * each item.
 *
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class IndexItemVisitor implements ItemVisitor {
  private final Visitor<Record> visitor;

  private final BoundingBoxDoubleGf envelope;

  public IndexItemVisitor(final BoundingBoxDoubleGf envelope, final Visitor<Record> visitor) {
    this.envelope = envelope;
    this.visitor = visitor;
  }

  @Override
  public void visitItem(final Object item) {
    final Record object = (Record)item;
    final BoundingBox envelope = object.getGeometry().getBoundingBox();
    if (envelope.intersects(this.envelope)) {
      this.visitor.visit(object);
    }
  }
}
