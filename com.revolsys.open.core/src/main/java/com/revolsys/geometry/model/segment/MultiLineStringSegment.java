package com.revolsys.geometry.model.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.vertex.Vertex;

public class MultiLineStringSegment extends AbstractSegment implements Iterator<Segment> {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private int segmentIndex;

  private int partIndex;

  public MultiLineStringSegment(final MultiLineString line, final int... segmentId) {
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
        return part.getCoordinate(this.segmentIndex + vertexIndex, axisIndex);
      }
    }
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final MultiLineString line = getMultiLineString();
    if (index == 0) {
      return line.getVertex(this.partIndex, this.segmentIndex);
    } else if (index == 1) {
      return line.getVertex(this.partIndex, this.segmentIndex + 1);
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
      return multiLine.getGeometry(this.partIndex);
    }
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      this.partIndex, this.segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return this.segmentIndex;
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
  public boolean isLineClosed() {
    return getPart().isClosed();
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getPart();
    return this.segmentIndex == line.getSegmentCount() - 1;
  }

  @Override
  public boolean isLineStart() {
    return this.segmentIndex == 0;
  }

  @Override
  public Segment next() {
    final MultiLineString multiLineString = getMultiLineString();
    this.segmentIndex++;
    while (this.partIndex < multiLineString.getGeometryCount()) {
      final LineString part = getPart();
      if (this.segmentIndex < part.getSegmentCount()) {
        return this;
      } else {
        this.partIndex++;
        this.segmentIndex = 0;
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  @Override
  public void setSegmentId(final int... segmentId) {
    this.partIndex = segmentId[0];
    this.segmentIndex = segmentId[1];
  }
}
