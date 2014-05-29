package com.revolsys.jts.geom.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.vertex.Vertex;

public class MultiLineStringSegment extends AbstractSegment implements
  Iterator<Segment> {
  private int segmentIndex;

  private int partIndex;

  public MultiLineStringSegment(final MultiLineString line,
    final int... segmentId) {
    super(line);
    setSegmentId(segmentId);
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString part = getPart();
      if (part == null) {
        return Double.NaN;
      } else {
        return part.getCoordinate(segmentIndex + vertexIndex, axisIndex);
      }
    }
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final MultiLineString line = getMultiLineString();
    if (index == 0) {
      return line.getVertex(partIndex, segmentIndex);
    } else if (index == 1) {
      return line.getVertex(partIndex, segmentIndex + 1);
    } else {
      return null;
    }
  }

  public MultiLineString getMultiLineString() {
    return (MultiLineString)getGeometry();
  }

  public LineString getPart() {
    final MultiLineString multiLine = getMultiLineString();
    if (multiLine == null) {
      return null;
    } else {
      return multiLine.getGeometry(partIndex);
    }
  }

  @Override
  public int getPartIndex() {
    return partIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      partIndex, segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return segmentIndex;
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final MultiLineString line = getMultiLineString();
      int partIndex = this.partIndex;
      int segmentIndex = this.segmentIndex + 1;
      while (partIndex < line.getGeometryCount()) {
        final LineString part = line.getGeometry(partIndex);
        if (segmentIndex < part.getSegmentCount()) {
          return true;
        } else {
          partIndex++;
          segmentIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getPart();
    return segmentIndex == line.getSegmentCount();
  }

  @Override
  public boolean isLineStart() {
    return segmentIndex == 0;
  }

  @Override
  public Segment next() {
    final MultiLineString multiLineString = getMultiLineString();
    segmentIndex++;
    while (partIndex < multiLineString.getGeometryCount()) {
      final LineString part = getPart();
      if (segmentIndex < part.getSegmentCount()) {
        return this;
      } else {
        partIndex++;
        segmentIndex = 0;
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  public void setSegmentId(final int... segmentId) {
    this.partIndex = segmentId[0];
    this.segmentIndex = segmentId[1];
  }
}
