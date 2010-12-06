package com.revolsys.gis.algorithm.index;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.visitor.Visitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;

/**
 * A {@link ItemVisitor} implementation which uses a {@link Visitor} to visit
 * each item.
 * 
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class IndexItemVisitor implements ItemVisitor {
  private final Visitor<DataObject> visitor;

  private Envelope envelope;

  public IndexItemVisitor(Envelope envelope, final Visitor<DataObject> visitor) {
    this.envelope = envelope;
    this.visitor = visitor;
  }

  public void visitItem(final Object item) {
    DataObject object = (DataObject)item;
    Envelope envelope = object.getGeometryValue().getEnvelopeInternal();
    if (envelope.intersects(this.envelope)) {
      visitor.visit(object);
    }
  }
}
