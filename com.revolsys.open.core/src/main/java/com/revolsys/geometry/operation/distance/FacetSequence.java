package com.revolsys.geometry.operation.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public interface FacetSequence extends Geometry {

  double distance(final FacetSequence sequence);

  double getCoordinate(int vertexIndex, int axisIndex);

  Point getPoint(int vertexIndex);

  @Override
  int getVertexCount();

  boolean isPoint();
}
