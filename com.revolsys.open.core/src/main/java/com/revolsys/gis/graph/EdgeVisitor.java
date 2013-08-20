package com.revolsys.gis.graph;

import com.revolsys.visitor.DelegatingVisitor;
import com.vividsolutions.jts.geom.Envelope;

public abstract class EdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public abstract Envelope getEnvelope();
}
