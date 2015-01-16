package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.MultiPoint;

public class MultiPointVertex extends AbstractVertex {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private int partIndex;

  public MultiPointVertex(final MultiPoint geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final MultiPoint geometry = getGeometry();
    return geometry.getCoordinate(this.partIndex, axisIndex);
  }

  public MultiPoint getMultiPoint() {
    return (MultiPoint)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex, 0
    };
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  @Override
  public boolean hasNext() {
    final MultiPoint multiPoint = getMultiPoint();
    if (multiPoint.isEmpty()) {
      return false;
    } else {
      if (this.partIndex + 1 < multiPoint.getGeometryCount()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public Vertex next() {
    final MultiPoint multiPoint = getMultiPoint();
    this.partIndex++;
    if (this.partIndex < multiPoint.getGeometryCount()) {
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setPartIndex(final int partIndex) {
    this.partIndex = partIndex;
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
  }
}
