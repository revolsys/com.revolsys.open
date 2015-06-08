package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.util.CollectionUtil;

public class CoordinateInfiniteError extends VertexCoordinateError {
  public CoordinateInfiniteError(final Vertex vertex, final int axisIndex) {
    super("Coordinate value " + GeometryFactory.getAxisName(axisIndex) + "="
      + vertex.getCoordinate(axisIndex) + " is invalid for vertex "
      + CollectionUtil.toString(",", vertex.getVertexId()), vertex, axisIndex);
  }
}
