package com.revolsys.gis.jts;

import java.util.Iterator;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateSequenceIndexLineSegmentIterator implements
  Iterator<CoordinateSequenceIndexLineSegment>,
  Iterable<CoordinateSequenceIndexLineSegment> {
  CoordinateSequenceIndexLineSegment coordinates;

  public CoordinateSequenceIndexLineSegmentIterator(
    final CoordinateSequence coordinateSequence) {
    this.coordinates = new CoordinateSequenceIndexLineSegment(
      coordinateSequence);
  }

  public boolean hasNext() {
    return coordinates.getIndex() < coordinates.size() - 1;
  }

  public Iterator<CoordinateSequenceIndexLineSegment> iterator() {
    return this;
  }

  public CoordinateSequenceIndexLineSegment next() {
    coordinates.setIndex(coordinates.getIndex() + 1);
    return coordinates;
  }

  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");
  }
}
