package com.revolsys.geometry.operation.distance;

import com.revolsys.geometry.model.Point;

public interface FacetSequence {

  double distance(final FacetSequence sequence);

  double getCoordinate(int vertexIndex, int axisIndex);

  Point getPoint(int vertexIndex);

  int getVertexCount();

  boolean isPoint();
}
