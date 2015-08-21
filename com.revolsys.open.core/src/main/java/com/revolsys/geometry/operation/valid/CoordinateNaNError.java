package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.CollectionUtil;

public class CoordinateNaNError extends VertexCoordinateError {
  public CoordinateNaNError(final Vertex vertex, final int axisIndex) {
    super("Coordinate value " + GeometryFactory.getAxisName(axisIndex)
      + "=NaN is invalid for vertex " + CollectionUtil.toString(",", vertex.getVertexId()), vertex,
      axisIndex);
  }

}
