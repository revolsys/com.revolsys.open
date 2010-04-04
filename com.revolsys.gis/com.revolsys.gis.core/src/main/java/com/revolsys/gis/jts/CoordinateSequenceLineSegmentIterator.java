package com.revolsys.gis.jts;

import java.util.Iterator;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateSequenceLineSegmentIterator implements
  Iterator<LineSegment3D>, Iterable<LineSegment3D> {
  private final CoordinateSequence coordinates;

  private int index = 0;

  public CoordinateSequenceLineSegmentIterator(
    final CoordinateSequence coordinates) {
    this.coordinates = coordinates;
  }

  public boolean hasNext() {
    return index < coordinates.size() - 1;
  }

  public Iterator<LineSegment3D> iterator() {
    return this;
  }

  public LineSegment3D next() {
    final LineSegment3D segment = new LineSegment3D(
      coordinates.getCoordinate(index), coordinates.getCoordinate(index + 1));
    index++;
    return segment;
  }

  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");

  }
}
