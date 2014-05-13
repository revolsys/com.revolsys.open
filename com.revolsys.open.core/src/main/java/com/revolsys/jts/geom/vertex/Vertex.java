package com.revolsys.jts.geom.vertex;

import java.util.Iterator;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

public interface Vertex extends Point, Iterator<Vertex> {

  @Override
  Vertex clone();

  @Override
  BoundingBox getBoundingBox();

  <V extends Geometry> V getGeometry();

  @Override
  GeometryFactory getGeometryFactory();

  int getPartIndex();

  int getRingIndex();

  @Override
  int getSrid();

  int[] getVertexId();

  int getVertexIndex();

  @Override
  boolean isEmpty();

  Reader<Vertex> reader();
}
