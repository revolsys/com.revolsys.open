package com.revolsys.jts.geom.vertex;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

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
