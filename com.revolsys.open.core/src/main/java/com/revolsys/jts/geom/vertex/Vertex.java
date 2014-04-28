package com.revolsys.jts.geom.vertex;

import java.util.Iterator;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

public interface Vertex extends Coordinates, Iterator<Vertex> {

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

  Reader<Vertex> reader();

  Point toPoint();
}
