package com.revolsys.jts.geom.segment;

import java.util.Iterator;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;

public interface Segment extends LineSegment, Iterator<Segment> {

  @Override
  Segment clone();

  <V extends Geometry> V getGeometry();

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
