package com.revolsys.jts.operation.distance;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;

public interface FacetSequence {

  double distance(final FacetSequence sequence);

  Point getCoordinate(int vertexIndex);

  double getCoordinate(int vertexIndex, int axisIndex);

  BoundingBox getEnvelope();

  int getVertexCount();

  boolean isPoint();
}
