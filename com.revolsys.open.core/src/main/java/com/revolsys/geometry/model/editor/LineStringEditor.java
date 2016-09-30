package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.AbstractLineString;
import com.revolsys.util.number.Doubles;

public class LineStringEditor extends AbstractLineString implements LinealEditor {
  private static final long serialVersionUID = 1L;

  private final LineString line;

  private double[] newCoordinates;

  private final int newVertexCount;

  private GeometryFactory newGeometryFactory;

  public LineStringEditor(final LineString line) {
    this.line = line;
    this.newVertexCount = line.getVertexCount();
    this.newGeometryFactory = line.getGeometryFactory();
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (this.newCoordinates == null) {
      return this.line.getCoordinate(vertexIndex, axisIndex);
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex < 0 || axisIndex >= axisCount) {
        return Double.NaN;
      } else {
        final int vertexCount = getVertexCount();
        if (vertexIndex < vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount;
          }
          final int coordinateIndex = vertexIndex * axisCount + axisIndex;
          return this.newCoordinates[coordinateIndex];
        } else {
          return Double.NaN;
        }
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (this.newCoordinates == null) {
      return this.line.getCoordinates();
    } else {
      final double[] coordinates = new double[this.newCoordinates.length];
      System.arraycopy(this.newCoordinates, 0, coordinates, 0, coordinates.length);
      return coordinates;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.newGeometryFactory;
  }

  @Override
  public int getVertexCount() {
    return this.newVertexCount;
  }

  @Override
  public boolean isEmpty() {
    return this.line.isEmpty();
  }

  @Override
  public LineString newGeometry() {
    if (this.newCoordinates == null) {
      return this.line;
    } else {
      return this.line.newLineString(this.newGeometryFactory, getAxisCount(), getVertexCount(),
        this.newCoordinates);
    }
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    return this.line.newLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.newCoordinates = getCoordinates(axisCount);
      this.newGeometryFactory = this.newGeometryFactory.convertAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      return setCoordinate(vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setCoordinate(final int vertexIndex, final int axisIndex, final double coordinate) {
    final int vertexCount = getVertexCount();
    if (vertexIndex >= 0 && vertexIndex < vertexCount) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        final double oldValue = this.line.getCoordinate(vertexIndex, axisIndex);
        if (!Doubles.equal(coordinate, oldValue)) {
          if (this.newCoordinates == null) {
            this.newCoordinates = this.line.getCoordinates(axisCount);
          }
          final int coordinateIndex = vertexIndex * axisCount + axisIndex;
          this.newCoordinates[coordinateIndex] = coordinate;
          return oldValue;
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setM(final int vertexIndex, final double m) {
    return setCoordinate(vertexIndex, M, m);
  }

  public double setX(final int vertexIndex, final double x) {
    return setCoordinate(vertexIndex, X, x);
  }

  public double setY(final int vertexIndex, final double y) {
    return setCoordinate(vertexIndex, Y, y);
  }

  public double setZ(final int vertexIndex, final double z) {
    return setCoordinate(vertexIndex, Z, z);
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
