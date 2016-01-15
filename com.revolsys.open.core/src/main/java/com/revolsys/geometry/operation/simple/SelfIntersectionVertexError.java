package com.revolsys.geometry.operation.simple;

import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.valid.VertexError;

public class SelfIntersectionVertexError extends VertexError {
  public SelfIntersectionVertexError(final Vertex vertex) {
    super("Self Intersection at Vertex", vertex);
  }
}
