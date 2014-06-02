package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;

public class PolygonVertex extends AbstractVertex {
  private int vertexIndex;

  private int ringIndex;

  public PolygonVertex(final Polygon polygon, final int... vertexId) {
    super(polygon);
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
        return new PolygonVertex(getPolygon(), ringIndex, newVertexIndex);
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
      return new PolygonVertex(getPolygon(), ringIndex, newVertexIndex);
    }
    return null;
  }

  public Polygon getPolygon() {
    return (Polygon)getGeometry();
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
      ringIndex, vertexIndex
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
      final Polygon polygon = getPolygon();
      int ringIndex = this.ringIndex;
      int vertexIndex = this.vertexIndex;
      while (ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(ringIndex);
        if (vertexIndex + 1 < ring.getVertexCount()) {
          return true;
        } else {
          ringIndex++;
          vertexIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public Vertex next() {
    final Polygon polygon = getPolygon();
    vertexIndex++;
    while (ringIndex < polygon.getRingCount()) {
      final LinearRing ring = polygon.getRing(ringIndex);
      if (vertexIndex < ring.getVertexCount()) {
        return this;
      } else {
        ringIndex++;
        vertexIndex = 0;
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setVertexId(final int... vertexId) {
    this.ringIndex = vertexId[0];
    this.vertexIndex = vertexId[1];
  }
}
