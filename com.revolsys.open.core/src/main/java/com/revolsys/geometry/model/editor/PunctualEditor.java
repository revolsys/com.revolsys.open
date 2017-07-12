package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Punctual;

public interface PunctualEditor extends GeometryEditor {

  @Override
  Punctual newGeometry();

  double setCoordinate(int partIndex, int axisIndex, double coordinate);

  default double setM(final int partIndex, final double m) {
    return setCoordinate(partIndex, M, m);
  }

  default double setX(final int partIndex, final double x) {
    return setCoordinate(partIndex, X, x);
  }

  default double setY(final int partIndex, final double y) {
    return setCoordinate(partIndex, Y, y);
  }

  default double setZ(final int partIndex, final double z) {
    return setCoordinate(partIndex, Z, z);
  }
}
