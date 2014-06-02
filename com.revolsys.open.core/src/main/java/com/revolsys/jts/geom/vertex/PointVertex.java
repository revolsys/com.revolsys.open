package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.Point;

public class PointVertex extends AbstractVertex {
  private int vertexIndex;

  public PointVertex(final Point geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int vertexIndex) {
    final Point point = getPoint();
    return point.getCoordinate(vertexIndex);
  }

  @Override
  public Point getPoint() {
    return (Point)getGeometry();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      0
    };
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  @Override
  public boolean hasNext() {
    final Point point = getPoint();
    if (point == null || point.isEmpty()) {
      return false;
    } else if (vertexIndex == -1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isFrom() {
    return vertexIndex == 0;
  }

  @Override
  public boolean isTo() {
    return vertexIndex == 0;
  }

  @Override
  public Vertex next() {
    if (hasNext()) {
      vertexIndex++;
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setVertexId(final int... vertexId) {
    if (vertexId.length == 1) {
      this.vertexIndex = vertexId[0];
    } else {
      this.vertexIndex = 1;
    }
  }
}
