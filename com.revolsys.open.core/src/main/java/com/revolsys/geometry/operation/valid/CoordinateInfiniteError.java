package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.Strings;

public class CoordinateInfiniteError extends VertexCoordinateError {
  public CoordinateInfiniteError(final Vertex vertex, final int axisIndex) {
    super("Coordinate value " + GeometryFactory.getAxisName(axisIndex) + "="
      + vertex.getCoordinate(axisIndex) + " is invalid for vertex "
      + Strings.toString(",", vertex.getVertexId()), vertex, axisIndex);
  }
}
