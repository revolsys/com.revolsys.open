package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;

public class LineStringVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int vertexIndex;

  public LineStringVertex(final LineString line, final int... vertexId) {
    super(line);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int index) {
    final LineString line = getGeometry();
    return line.getCoordinate(this.vertexIndex, index);
  }

  @Override
  public Vertex getLineNext() {
    final int newVertexIndex = this.vertexIndex + 1;
    final int vertexCount = getLineString().getVertexCount();
    if (newVertexIndex < vertexCount) {
      return new LineStringVertex(getLineString(), newVertexIndex);
    } else {
      return null;
    }
  }

  @Override
  public Vertex getLinePrevious() {
    final int newVertexIndex = this.vertexIndex - 1;
    if (newVertexIndex < 0) {
      return null;
    } else {
      return new LineStringVertex(getLineString(), newVertexIndex);
    }
  }

  public LineString getLineString() {
    return (LineString)getGeometry();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.vertexIndex
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
  public boolean isFrom() {
    return getVertexIndex() == 0;
  }

  @Override
  public boolean isTo() {
    final int vertexIndex = getVertexIndex();
    final LineString lineString = getLineString();
    final int lastVertexIndex = lineString.getVertexCount() - 1;
    return vertexIndex == lastVertexIndex;
  }

  @Override
  public Vertex next() {
    final LineString lineString = getLineString();
    this.vertexIndex++;
    if (this.vertexIndex < lineString.getVertexCount()) {
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
