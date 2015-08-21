package com.revolsys.geometry.operation.distance;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;

public interface FacetSequence {

  double distance(final FacetSequence sequence);

  Point getCoordinate(int vertexIndex);

  double getCoordinate(int vertexIndex, int axisIndex);

  BoundingBox getEnvelope();

  int getVertexCount();

  boolean isPoint();
}
