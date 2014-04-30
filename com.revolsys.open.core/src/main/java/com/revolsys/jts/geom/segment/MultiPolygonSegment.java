package com.revolsys.jts.geom.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;

public class MultiPolygonSegment extends AbstractSegment implements
  Iterator<Segment> {
  private int segmentIndex;

  private int ringIndex;

  private int partIndex;

  public MultiPolygonSegment(final MultiPolygon multiPolygon,
    final int... segmentId) {
    super(multiPolygon);
    setSegmentId(segmentId);
  }

  public MultiPolygon getMultiPolygon() {
    return (MultiPolygon)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return partIndex;
  }

  public Polygon getPolygon() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    if (multiPolygon == null) {
      return null;
    } else {
      return multiPolygon.getPolygon(partIndex);
    }
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    if (polygon == null) {
      return null;
    } else {
      return polygon.getRing(ringIndex);
    }
  }

  @Override
  public int getRingIndex() {
    return ringIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      partIndex, ringIndex, segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return segmentIndex;
  }

  @Override
  public double getValue(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getCoordinate(segmentIndex + vertexIndex, axisIndex);
      }
    }
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
  public Segment next() {
    segmentIndex++;
    final MultiPolygon multiPolygon = getMultiPolygon();
    while (partIndex < multiPolygon.getGeometryCount()) {
      final Polygon polygon = getPolygon();
      while (ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(ringIndex);
        if (segmentIndex < ring.getSegmentCount()) {
          return this;
        } else {
          ringIndex++;
          segmentIndex = 0;
        }
      }
      partIndex++;
      ringIndex = 0;
      segmentIndex = 0;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  public void setSegmentId(final int... segmentId) {
    this.partIndex = segmentId[0];
    this.ringIndex = segmentId[1];
    this.segmentIndex = segmentId[2];
  }
}
