package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.util.CollectionUtil;

public class CoordinateNaNError extends VertexCoordinateError {
  public CoordinateNaNError(final Vertex vertex, final int axisIndex) {
    super("Coordinate value " + GeometryFactory.getAxisName(axisIndex)
      + "=NaN is invalid for vertex " + CollectionUtil.toString(",", vertex.getVertexId()), vertex,
      axisIndex);
  }

}
