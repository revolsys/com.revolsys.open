package com.revolsys.jts.geom.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;

public class PolygonSegment extends AbstractSegment implements
  Iterator<Segment> {
  private int segmentIndex;

  private int ringIndex;

  public PolygonSegment(final Polygon polygon, final int... segmentId) {
    super(polygon);
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
        return ring.getCoordinate(segmentIndex + vertexIndex, axisIndex);
      }
    }
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final Polygon polygon = getPolygon();
    if (index == 0) {
      return polygon.getVertex(ringIndex, segmentIndex);
    } else if (index == 1) {
      return polygon.getVertex(ringIndex, segmentIndex + 1);
    } else {
      return null;
    }
  }

  public Polygon getPolygon() {
    return (Polygon)getGeometry();
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
      ringIndex, segmentIndex
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
      final Polygon polygon = getPolygon();
      int ringIndex = this.ringIndex;
      int segmentIndex = this.segmentIndex;
      while (ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(ringIndex);
        if (segmentIndex + 1 < ring.getSegmentCount()) {
          return true;
        } else {
          ringIndex++;
          segmentIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getRing();
    return segmentIndex == line.getSegmentCount();
  }

  @Override
  public boolean isLineStart() {
    return segmentIndex == 0;
  }

  @Override
  public Segment next() {
    final Polygon polygon = getPolygon();
    segmentIndex++;
    while (ringIndex < polygon.getRingCount()) {
      final LinearRing ring = polygon.getRing(ringIndex);
      if (segmentIndex < ring.getSegmentCount()) {
        return this;
      } else {
        ringIndex++;
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
    this.ringIndex = segmentId[0];
    this.segmentIndex = segmentId[1];
  }
}
