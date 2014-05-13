package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.LineString;

public class LineStringVertex extends AbstractVertex {
  private int vertexIndex;

  public LineStringVertex(final LineString line, final int... vertexId) {
    super(line);
    setVertexId(vertexId);
  }

  public LineString getLineString() {
    return (LineString)getGeometry();
  }

  @Override
  public double getCoordinate(final int index) {
    final LineString line = getGeometry();
    return line.getCoordinate(vertexIndex, index);
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      vertexIndex
    };
  }

  @Override
  public boolean hasNext() {
    final LineString lineString = getLineString();
    if (lineString.isEmpty()) {
      return false;
    } else {
      if (this.vertexIndex + 1 < lineString.getVertexCount()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public Vertex next() {
    final LineString lineString = getLineString();
    vertexIndex++;
    if (vertexIndex < lineString.getVertexCount()) {
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
    this.vertexIndex = vertexId[0];
  }

  public void setVertexIndex(final int vertexIndex) {
    this.vertexIndex = vertexIndex;
  }

}
