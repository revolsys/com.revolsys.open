package com.revolsys.gis.graph;

import com.revolsys.jts.geom.Envelope;
import com.revolsys.visitor.DelegatingVisitor;

public abstract class EdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public abstract Envelope getEnvelope();
}
