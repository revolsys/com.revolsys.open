package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.index.ItemVisitor;

/**
 * A {@link ItemVisitor} implementation which uses a {@link Visitor} to visit
 * each item.
 * 
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class IndexItemVisitor implements ItemVisitor {
  private final Visitor<DataObject> visitor;

  private final Envelope envelope;

  public IndexItemVisitor(final Envelope envelope,
    final Visitor<DataObject> visitor) {
    this.envelope = envelope;
    this.visitor = visitor;
  }

  @Override
  public void visitItem(final Object item) {
    final DataObject object = (DataObject)item;
    final BoundingBox envelope = object.getGeometryValue().getBoundingBox();
    if (envelope.intersects(this.envelope)) {
      visitor.visit(object);
    }
  }
}
