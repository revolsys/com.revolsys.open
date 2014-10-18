package com.revolsys.jts.operation.simple;

import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.valid.VertexError;

public class SelfIntersectionVertexError extends VertexError {
  public SelfIntersectionVertexError(final Vertex vertex) {
    super("Self Intersection at Vertex", vertex);
  }
}
