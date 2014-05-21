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

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString lineString = getLineString();
      return lineString.getCoordinate(segmentIndex + vertexIndex, axisIndex);
    }
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
  public boolean hasNext() {
    final LineString line = getLineString();
    if (line.isEmpty()) {
      return false;
    } else {
      final int segmentIndex = this.segmentIndex;
      if (segmentIndex + 1 < line.getSegmentCount()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getLineString();
    return segmentIndex == line.getSegmentCount();
  }

  @Override
  public boolean isLineStart() {
    return segmentIndex == 0;
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
