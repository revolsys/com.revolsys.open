package com.revolsys.geometry.model.vertex;

import java.util.Iterator;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.Reader;

public interface Vertex extends Point, Iterator<Vertex>, GeometryComponent {

  @Override
  Vertex clone();

  @Override
  BoundingBox getBoundingBox();

  <V extends Geometry> V getGeometry();

  @Override
  GeometryFactory getGeometryFactory();

  Vertex getLineNext();

  Vertex getLinePrevious();

  int getPartIndex();

  int getRingIndex();

  @Override
  int getSrid();

  int[] getVertexId();

  int getVertexIndex();

  @Override
  boolean isEmpty();

  boolean isFrom();

  boolean isTo();

  Reader<Vertex> reader();
}
