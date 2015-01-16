package com.revolsys.gis.jts;

import java.util.Iterator;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

public class LineStringCoordinatesListIterator implements Iterator<LineString>,
Iterable<LineString> {
  private final GeometryFactory factory;

  private int index = 0;

  private final LineString points;

  public LineStringCoordinatesListIterator(final GeometryFactory factory,
    final LineString points) {
    this.factory = factory;
    this.points = points;
  }

  public LineStringCoordinatesListIterator(final LineString line) {
    this(line.getGeometryFactory(), line);
  }

  @Override
  public boolean hasNext() {
    return this.index < this.points.getVertexCount() - 1;
  }

  @Override
  public Iterator<LineString> iterator() {
    return this;
  }

  @Override
  public LineString next() {
    final LineString lineString = this.factory.lineString(this.points.subLine(this.index, 2));
    this.index++;
    return lineString;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");

  }
}
