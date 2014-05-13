package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

public class MultiLineStringVertex extends AbstractVertex {
  private int vertexIndex;

  private int partIndex;

  public MultiLineStringVertex(final MultiLineString multiLineString,
    final int... vertexId) {
    super(multiLineString);
    setVertexId(vertexId);
  }

  public LineString getLineString() {
    final MultiLineString multiLineString = getMultiLineString();
    return multiLineString.getLineString(partIndex);
  }

  public MultiLineString getMultiLineString() {
    return (MultiLineString)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  @Override
  public double getCoordinate(final int index) {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return Double.NaN;
    } else {
      return lineString.getCoordinate(vertexIndex, index);
    }
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      partIndex, vertexIndex
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
      final MultiLineString multiLineString = getMultiLineString();
      int partIndex = this.partIndex;
      int vertexIndex = this.vertexIndex + 1;

      while (partIndex < multiLineString.getGeometryCount()) {
        final LineString lineString = multiLineString.getLineString(partIndex);

        if (vertexIndex < lineString.getVertexCount()) {
          return true;
        } else {
          partIndex++;
          vertexIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public Vertex next() {
    final MultiLineString multiLineString = getMultiLineString();
    vertexIndex++;
    while (partIndex < multiLineString.getGeometryCount()) {
      final LineString lineString = multiLineString.getLineString(partIndex);
      if (vertexIndex < lineString.getVertexCount()) {
        return this;
      } else {
        partIndex++;
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
    this.partIndex = vertexId[0];
    this.vertexIndex = vertexId[1];
  }
}
