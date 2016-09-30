package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Lineal;

public interface LinealEditor extends GeometryEditor {
  @Override
  Lineal newGeometry();

  double setCoordinate(int partIndex, int vertexIndex, int axisIndex, double coordinate);

  default double setM(final int partIndex, final int vertexIndex, final double m) {
    return setCoordinate(partIndex, vertexIndex, M, m);
  }

  default double setX(final int partIndex, final int vertexIndex, final double x) {
    return setCoordinate(partIndex, vertexIndex, X, x);
  }

  default double setY(final int partIndex, final int vertexIndex, final double y) {
    return setCoordinate(partIndex, vertexIndex, Y, y);
  }

  default double setZ(final int partIndex, final int vertexIndex, final double z) {
    return setCoordinate(partIndex, vertexIndex, Z, z);
  }
}
