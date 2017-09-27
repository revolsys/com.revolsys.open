package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public interface GeometryEditor extends Geometry {
  boolean isModified();

  Geometry newGeometry();

  int setAxisCount(int axisCount);

  double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId);

  default double setM(final double m, final int... vertexId) {
    return setCoordinate(M, m, vertexId);
  }

  default void setVertex(final Point newPoint, final int... vertexId) {
    final int axisCount = getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = newPoint.getCoordinate(axisIndex);
      setCoordinate(axisIndex, coordinate, vertexId);
    }
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
