package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.geometry.LineSegment;

public class CoordinatesListIndexLineSegmentIterator implements
  Iterator<LineSegment>, Iterable<LineSegment> {
  private static final GeometryFactory FACTORY = GeometryFactory.getFactory();

  private final GeometryFactory factory;

  private final CoordinatesList points;

  private int index = -1;

  public CoordinatesListIndexLineSegmentIterator(final CoordinatesList points) {
    this(FACTORY, points);
  }

  public CoordinatesListIndexLineSegmentIterator(final GeometryFactory factory,
    final CoordinatesList points) {
    this.factory = factory;
    this.points = points;
  }

  public boolean hasNext() {
    return index < points.size() - 2;
  }

  public Iterator<LineSegment> iterator() {
    return this;
  }

  public LineSegment next() {
    index++;
    return new LineSegment(factory, points.get(index), points.get(index + 1));
  }

  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");
  }
}
