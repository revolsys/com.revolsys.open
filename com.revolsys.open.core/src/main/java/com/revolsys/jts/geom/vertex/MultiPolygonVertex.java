package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;

public class MultiPolygonVertex extends AbstractVertex {
  private int vertexIndex;

  private int partIndex;

  private int ringIndex;

  public MultiPolygonVertex(final MultiPolygon multiPolygon,
    final int... vertexId) {
    super(multiPolygon);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int index) {
    final Polygon polygon = getPolygon();
    final LinearRing ring = polygon.getRing(ringIndex);
    if (ring == null) {
      return Double.NaN;
    } else {
      return ring.getCoordinate(vertexIndex, index);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = vertexIndex + 1;
      if (newVertexIndex >= ring.getVertexCount() - 1) {
        newVertexIndex -= ring.getVertexCount();
      }
      if (newVertexIndex < ring.getVertexCount() - 1) {
        return new MultiPolygonVertex(getMultiPolygon(), partIndex, ringIndex,
          newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = vertexIndex - 1;
      if (newVertexIndex == -1) {
        newVertexIndex = ring.getVertexCount() - 2;
      }
      if (newVertexIndex >= 0) {
        return new MultiPolygonVertex(getMultiPolygon(), partIndex, ringIndex,
          newVertexIndex);
      }
    }
    return null;
  }

  public MultiPolygon getMultiPolygon() {
    return (MultiPolygon)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  public Polygon getPolygon() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    return multiPolygon.getPolygon(partIndex);
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    return polygon.getRing(ringIndex);
  }

  @Override
  public int getRingIndex() {
    return ringIndex;
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      partIndex, ringIndex, vertexIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return vertexIndex;
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final MultiPolygon multiPolygon = getMultiPolygon();
      int partIndex = this.partIndex;
      int ringIndex = this.ringIndex;
      int vertexIndex = this.vertexIndex + 1;

      while (partIndex < multiPolygon.getGeometryCount()) {
        final Polygon polygon = multiPolygon.getPolygon(partIndex);

        while (ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          if (vertexIndex < ring.getVertexCount()) {
            return true;
          } else {
            ringIndex++;
            vertexIndex = 0;
          }
        }
        partIndex++;
        ringIndex = 0;
        vertexIndex = 0;
      }
      return false;
    }
  }

  @Override
  public Vertex next() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    vertexIndex++;
    while (partIndex < multiPolygon.getGeometryCount()) {
      final Polygon polygon = multiPolygon.getPolygon(partIndex);
      while (ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(ringIndex);
        if (vertexIndex < ring.getVertexCount()) {
          return this;
        } else {
          ringIndex++;
          vertexIndex = 0;
        }
      }
      partIndex++;
      ringIndex = 0;
      vertexIndex = 0;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
    this.ringIndex = vertexId[1];
    this.vertexIndex = vertexId[2];
  }
}
