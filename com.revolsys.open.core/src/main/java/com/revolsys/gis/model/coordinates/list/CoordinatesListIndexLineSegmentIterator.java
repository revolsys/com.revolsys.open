package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;

import com.revolsys.gis.jts.LineSegmentDoubleGF;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

public class CoordinatesListIndexLineSegmentIterator implements
  Iterator<LineSegment>, Iterable<LineSegment> {
  private static final GeometryFactory FACTORY = GeometryFactory.floating3();

  private final GeometryFactory factory;

  private final PointList points;

  private int index = -1;

  public CoordinatesListIndexLineSegmentIterator(final PointList points) {
    this(FACTORY, points);
  }

  public CoordinatesListIndexLineSegmentIterator(final GeometryFactory factory,
    final PointList points) {
    this.factory = factory;
    this.points = points;
  }

  public CoordinatesListIndexLineSegmentIterator(final LineString line) {
    this.factory = line.getGeometryFactory();
    this.points = CoordinatesListUtil.get(line);
  }

  @Override
  public boolean hasNext() {
    return index < points.size() - 2;
  }

  @Override
  public Iterator<LineSegment> iterator() {
    return this;
  }

  @Override
  public LineSegment next() {
    index++;
    return new LineSegmentDoubleGF(factory, points.get(index),
      points.get(index + 1));
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");
  }
}
