package com.revolsys.geometry.model.edit;

import com.revolsys.geometry.model.Geometry;

public interface GeometryEditor extends Geometry {
  Geometry newGeometry();

  int setAxisCount(int axisCount);

  double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId);

  default double setM(final double m, final int... vertexId) {
    return setCoordinate(M, m, vertexId);
  }

  default double setX(final double x, final int... vertexId) {
    return setCoordinate(X, x, vertexId);
  }

  default double setY(final double y, final int... vertexId) {
    return setCoordinate(Y, y, vertexId);
  }

  default double setZ(final double z, final int... vertexId) {
    return setCoordinate(Z, z, vertexId);
  }
}
