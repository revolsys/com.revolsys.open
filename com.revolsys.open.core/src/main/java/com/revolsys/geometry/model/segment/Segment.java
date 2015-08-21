package com.revolsys.geometry.model.segment;

import java.util.Iterator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.Reader;

public interface Segment extends LineSegment, Iterator<Segment>, GeometryComponent {

  @Override
  Segment clone();

  <V extends Geometry> V getGeometry();

  Vertex getGeometryVertex(int index);

  int getPartIndex();

  int getRingIndex();

  int[] getSegmentId();

  int getSegmentIndex();

  boolean isLineClosed();

  boolean isLineEnd();

  boolean isLineStart();

  Reader<Segment> reader();

  void setSegmentId(int[] segmentId);
}
