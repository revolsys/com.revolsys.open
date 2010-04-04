package com.revolsys.gis.graph;

import com.revolsys.gis.data.visitor.NestedVisitor;
import com.vividsolutions.jts.geom.Envelope;

public abstract class EdgeVisitor<T> extends NestedVisitor<Edge<T>> {
  public abstract Envelope getEnvelope();
}
