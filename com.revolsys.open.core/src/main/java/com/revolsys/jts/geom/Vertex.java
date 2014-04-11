package com.revolsys.jts.geom;

import com.revolsys.gis.cs.BoundingBox;

public interface Vertex extends Coordinates {

  @Override
  Vertex clone();

  BoundingBox getBoundingBox();

  <V extends Geometry> V getGeometry();

  GeometryFactory getGeometryFactory();

  int getPartIndex();

  int getRingIndex();

  int getSrid();

  int[] getVertexId();

  int getVertexIndex();

  boolean isEmpty();

  Point toPoint();
}
