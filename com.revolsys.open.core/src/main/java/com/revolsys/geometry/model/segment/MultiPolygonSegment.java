package com.revolsys.geometry.model.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.vertex.Vertex;

public class MultiPolygonSegment extends AbstractSegment implements Iterator<Segment> {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private int segmentIndex;

  private int ringIndex;

  private int partIndex;

  public MultiPolygonSegment(final MultiPolygon multiPolygon, final int... segmentId) {
    super(multiPolygon);
    setSegmentId(segmentId);
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getCoordinate(this.segmentIndex + vertexIndex, axisIndex);
      }
    }
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final MultiPolygon polygon = getMultiPolygon();
    if (index == 0) {
      return polygon.getVertex(this.partIndex, this.ringIndex, this.segmentIndex);
    } else if (index == 1) {
      return polygon.getVertex(this.partIndex, this.ringIndex, this.segmentIndex + 1);
    } else {
      return null;
    }
  }

  public MultiPolygon getMultiPolygon() {
    return (MultiPolygon)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  public Polygon getPolygon() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    if (multiPolygon == null) {
      return null;
    } else {
      return multiPolygon.getPolygon(this.partIndex);
    }
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    if (polygon == null) {
      return null;
    } else {
      return polygon.getRing(this.ringIndex);
    }
  }

  @Override
  public int getRingIndex() {
    return this.ringIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      this.partIndex, this.ringIndex, this.segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  @Override
  public boolean hasNext() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    if (multiPolygon.isEmpty()) {
      return false;
    } else {
      int partIndex = this.partIndex;
      int ringIndex = this.ringIndex;
      int segmentIndex = this.segmentIndex + 1;
      while (partIndex < multiPolygon.getGeometryCount()) {
        final Polygon polygon = getPolygon();
        while (ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          if (segmentIndex < ring.getSegmentCount()) {
            return true;
          } else {
            ringIndex++;
            segmentIndex = 0;
          }
        }
        partIndex++;
        ringIndex = 0;
        segmentIndex = 0;
      }
      return false;
    }
  }

  @Override
  public boolean isLineClosed() {
    return true;
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getRing();
    return this.segmentIndex == line.getSegmentCount();
  }

  @Override
  public boolean isLineStart() {
    return this.segmentIndex == 0;
  }

  @Override
  public Segment next() {
    this.segmentIndex++;
    final MultiPolygon multiPolygon = getMultiPolygon();
    while (this.partIndex < multiPolygon.getGeometryCount()) {
      final Polygon polygon = getPolygon();
      while (this.ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(this.ringIndex);
        if (this.segmentIndex < ring.getSegmentCount()) {
          return this;
        } else {
          this.ringIndex++;
          this.segmentIndex = 0;
        }
      }
      this.partIndex++;
      this.ringIndex = 0;
      this.segmentIndex = 0;
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
    this.ringIndex = segmentId[1];
    this.segmentIndex = segmentId[2];
  }
}
