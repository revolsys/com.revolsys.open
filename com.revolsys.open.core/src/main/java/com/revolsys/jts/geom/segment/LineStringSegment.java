package com.revolsys.jts.geom.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;

public class LineStringSegment extends AbstractSegment implements
  Iterator<Segment> {
  private int segmentIndex;

  public LineStringSegment(final LineString line, final int... segmentId) {
    super(line);
    setSegmentId(segmentId);
  }

  public LineString getLineString() {
    return (LineString)getGeometry();
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      segmentIndex
    };
  }

  @Override
  public double getValue(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString lineString = getLineString();
      return lineString.getCoordinate(segmentIndex + vertexIndex, axisIndex);
    }
  }

  @Override
  public boolean hasNext() {
    final LineString lineString = getLineString();
    if (lineString.isEmpty()) {
      return false;
    } else {
      final int segmentIndex = this.segmentIndex;
      if (segmentIndex + 1 < lineString.getSegmentCount()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public Segment next() {
    final LineString lineString = getLineString();
    segmentIndex++;
    if (segmentIndex < lineString.getSegmentCount()) {
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  public void setSegmentId(final int... segmentId) {
    this.segmentIndex = segmentId[0];
  }

  public void setSegmentIndex(final int segmentIndex) {
    this.segmentIndex = segmentIndex;
  }

}
