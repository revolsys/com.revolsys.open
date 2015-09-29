package com.revolsys.geometry.model.vertex;

import java.util.Iterator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.Reader;

public interface Vertex extends Point, Iterator<Vertex>, GeometryComponent {
  @Override
  Vertex clone();

  <V extends Geometry> V getGeometry();

  Vertex getLineNext();

  Vertex getLinePrevious();

  int getPartIndex();

  int getRingIndex();

  int[] getVertexId();

  int getVertexIndex();

  boolean isFrom();

  boolean isTo();

  Reader<Vertex> reader();
}
