package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;

public class MultiLineStringVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int partIndex;

  private int vertexIndex;

  public MultiLineStringVertex(final MultiLineString multiLineString, final int... vertexId) {
    super(multiLineString);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return Double.NaN;
    } else {
      return lineString.getCoordinate(this.vertexIndex, axisIndex);
    }
  }

  @Override
  public double getLineCoordinateRelative(final int vertexOffset, final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int vertexIndex = getVertexIndex();
      final LineString line = getLineString();
      return line.getCoordinate(vertexIndex + vertexOffset, axisIndex);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString line = getLineString();
    if (line != null) {
      final int newVertexIndex = this.vertexIndex + 1;
      if (newVertexIndex < line.getVertexCount() - 1) {
        return new MultiLineStringVertex(getMultiLineString(), this.partIndex, newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    final LineString line = getLineString();
    if (line != null) {
      final int newVertexIndex = this.vertexIndex - 1;
      if (newVertexIndex >= 0) {
        return new MultiLineStringVertex(getMultiLineString(), this.partIndex, newVertexIndex);
      }
    }
    return null;
  }

  public LineString getLineString() {
    final MultiLineString multiLineString = getMultiLineString();
    return multiLineString.getLineString(this.partIndex);
  }

  public MultiLineString getMultiLineString() {
    return (MultiLineString)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex, this.vertexIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return this.vertexIndex;
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
    final MultiLineString multiLineString = getMultiLineString();
    this.vertexIndex++;
    while (this.partIndex < multiLineString.getGeometryCount()) {
      final LineString lineString = multiLineString.getLineString(this.partIndex);
      if (this.vertexIndex < lineString.getVertexCount()) {
        return this;
      } else {
        this.partIndex++;
        this.vertexIndex = 0;
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
