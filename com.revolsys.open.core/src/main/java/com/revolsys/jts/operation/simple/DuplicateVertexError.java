package com.revolsys.jts.operation.simple;

import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.valid.VertexError;

public class DuplicateVertexError extends VertexError {
  public DuplicateVertexError(final Vertex vertex) {
    super("Duplicate Vertex", vertex);
  }
}
