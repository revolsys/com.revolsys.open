package com.revolsys.geometry.model.segment;

import java.util.Iterator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public interface Segment
  extends LineSegment, Iterator<Segment>, Iterable<Segment>, GeometryComponent {
  @Override
  Segment clone();

  <V extends Geometry> V getGeometry();

  Vertex getGeometryVertex(int index);

  default int getPartIndex() {
    return -1;
  }

  default int getRingIndex() {
    return -1;
  }

  int[] getSegmentId();

  default int getSegmentIndex() {
    final int[] vertexId = getSegmentId();
    return vertexId[vertexId.length - 1];
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isEndIntersection(final Point point) {
    if (isLineStart()) {
      return equalsVertex(2, 0, point);
    } else if (isLineEnd()) {
      return equalsVertex(2, 1, point);
    } else {
      return false;
    }
  }

  boolean isLineClosed();

  boolean isLineEnd();

  boolean isLineStart();

  @Override
  default Iterator<Segment> iterator() {
    return this;
  }

  void setSegmentId(int... segmentId);
}
