package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.Vertex;

public class VertexError extends AbstractGeometryValidationError {

  private final int[] vertexId;

  public VertexError(final String message, final Vertex vertex) {
    super(message, vertex.getGeometry());
    this.vertexId = vertex.getVertexId();
  }

  @Override
  public Point getErrorPoint() {
    return getVertex();
  }

  public Vertex getVertex() {
    final Geometry geometry = getGeometry();
    final Vertex vertex = geometry.getVertex(this.vertexId);
    return vertex;
  }

  public int[] getVertexId() {
    return this.vertexId;
  }
}
