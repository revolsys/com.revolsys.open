package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Polygonal;

public interface PolygonalEditor extends GeometryEditor {
  @Override
  Polygonal newGeometry();

  Iterable<PolygonEditor> polygonEditors();

  double setCoordinate(int partIndex, int ringIndex, int vertexIndex, int axisIndex,
    double coordinate);

  default double setM(final int partIndex, final int ringIndex, final int vertexIndex,
    final double m) {
    return setCoordinate(partIndex, ringIndex, vertexIndex, M, m);
  }

  default double setX(final int partIndex, final int ringIndex, final int vertexIndex,
    final double x) {
    return setCoordinate(partIndex, ringIndex, vertexIndex, X, x);
  }

  default double setY(final int partIndex, final int ringIndex, final int vertexIndex,
    final double y) {
    return setCoordinate(partIndex, ringIndex, vertexIndex, Y, y);
  }

  default double setZ(final int partIndex, final int ringIndex, final int vertexIndex,
    final double z) {
    return setCoordinate(partIndex, ringIndex, vertexIndex, Z, z);
  }

}
