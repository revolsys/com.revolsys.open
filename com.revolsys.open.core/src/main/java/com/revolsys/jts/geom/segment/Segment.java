package com.revolsys.jts.geom.segment;

import java.util.Iterator;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponent;
import com.revolsys.jts.geom.vertex.Vertex;

public interface Segment extends LineSegment, Iterator<Segment>,
  GeometryComponent {

  @Override
  Segment clone();

  <V extends Geometry> V getGeometry();

  Vertex getGeometryVertex(int index);

  int getPartIndex();

  int getRingIndex();

  int[] getSegmentId();

  int getSegmentIndex();

  @Override
  int getSrid();

  @Override
  boolean isEmpty();

  boolean isLineEnd();

  boolean isLineStart();

  Reader<Segment> reader();
}
