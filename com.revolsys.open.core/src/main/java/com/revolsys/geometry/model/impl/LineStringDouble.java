package com.revolsys.geometry.model.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;

public class LineStringDouble extends AbstractLineString {
  private static final long serialVersionUID = 7579865828939708871L;

  private static final double[] EMPTY_COORDINATES = new double[0];

  public static LineStringDouble newLineStringDouble(final int axisCount, final int vertexCount,
    final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      return new LineStringDouble(axisCount);
    } else {
      assert axisCount >= 2;
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length=" + coordinates.length
          + " must be a multiple of axisCount=" + axisCount);
      } else if (coordinateCount == coordinates.length) {
        return new LineStringDouble(axisCount, vertexCount, coordinates);
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
          + vertexCount + " > coordinates.length=" + coordinates.length);
      } else {
        final double[] copyCoordinates = new double[coordinateCount];
        System.arraycopy(coordinates, 0, copyCoordinates, 0, coordinateCount);
        return new LineStringDouble(axisCount, vertexCount, copyCoordinates);
      }

    }
  }

  protected final int axisCount;

  protected final int vertexCount;

  protected double[] coordinates;

  public LineStringDouble(final int axisCount) {
    this.axisCount = axisCount;
    this.vertexCount = 0;
    this.coordinates = EMPTY_COORDINATES;
  }

  public LineStringDouble(final int axisCount, final Collection<Point> points) {
    this(points.size(), axisCount);
    int i = 0;
    for (final Point point : points) {
      CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, i++, point);
    }
  }

  public LineStringDouble(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = EMPTY_COORDINATES;
      this.vertexCount = 0;
    } else {
      assert axisCount >= 2;
      this.axisCount = axisCount;
      this.coordinates = coordinates;
      this.vertexCount = coordinates.length / axisCount;
    }
  }

  protected LineStringDouble(final int size, final int axisCount) {
    assert axisCount >= 2;
    assert size >= 0;
    this.coordinates = new double[size * axisCount];
    this.axisCount = axisCount;
    this.vertexCount = this.coordinates.length / axisCount;
  }

  public LineStringDouble(final int axisCount, final int vertexCount, final double... coordinates) {
    this.axisCount = axisCount;
    this.vertexCount = vertexCount;
    this.coordinates = coordinates;
  }

  public LineStringDouble(final int axisCount, final LineString points) {
    this(points.getVertexCount(), axisCount);
    CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, 0, points, 0,
      points.getVertexCount());
  }

  public LineStringDouble(final int axisCount, final List<? extends Number> coordinates) {
    this(axisCount, MathUtil.toDoubleArray(coordinates));
  }

  public LineStringDouble(final int axisCount, final Point... points) {
    this(axisCount, Arrays.asList(points));
  }

  public LineStringDouble(final LineString coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList);
  }

  public LineStringDouble(final Point... coordinates) {
    this(3, coordinates);
  }

  @Override
  public LineStringDouble clone() {
    final LineStringDouble clone = (LineStringDouble)super.clone();
    clone.coordinates = this.coordinates.clone();
    return clone;
  }

  @Override
  public double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    final double[] sourceCoordinates = this.coordinates;
    if (isEmpty()) {
      return sourceCoordinates;
    } else {
      final int sourceAxisCount = this.axisCount;
      final int vertexCount = this.vertexCount;
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      final int targetAxisCount = geometryFactory.getAxisCount();
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        if (sourceAxisCount == geometryFactory.getAxisCount()) {
          return sourceCoordinates;
        } else {
          final double[] coordinates = new double[targetAxisCount * vertexCount];
          int i = 0;
          for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            for (int axisIndex = 0; axisIndex < targetAxisCount; axisIndex++) {
              final double coordinate = sourceCoordinates[vertexIndex * sourceAxisCount
                + axisIndex];
              coordinates[i++] = coordinate;
            }
          }
          return coordinates;
        }
      } else {
        targetCoordinates = new double[targetAxisCount * vertexCount];
        coordinatesOperation.perform(sourceAxisCount, sourceCoordinates, targetAxisCount,
          targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @Override
  public int getAxisCount() {
    return this.axisCount;
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    final int axisCount = this.axisCount;
    if (axisIndex < axisCount) {
      final int vertexCount = getVertexCount();
      if (vertexIndex < vertexCount) {
        while (vertexIndex < 0) {
          vertexIndex += vertexCount;
        }
        return this.coordinates[vertexIndex * axisCount + axisIndex];
      }
    }
    return Double.NaN;
  }

  @Override
  public double getCoordinateFast(final int vertexIndex, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      return this.coordinates[vertexIndex * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    return this.coordinates.clone();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    final int axisCount = this.axisCount;
    if (axisCount == 2) {
      return GeometryFactory.DEFAULT_2D;
    } else if (axisCount == 2) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      return GeometryFactory.floating(0, this.axisCount);
    }
  }

  @Override
  public int getSegmentCount() {
    final int vertexCount = this.vertexCount;
    if (vertexCount == 0) {
      return 0;
    } else {
      return vertexCount - 1;
    }
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  @Override
  public double getX(final int vertexIndex) {
    return this.coordinates[vertexIndex * this.axisCount];
  }

  @Override
  public double getY(final int vertexIndex) {
    return this.coordinates[vertexIndex * this.axisCount + 1];
  }

  @Override
  public double getZ(final int vertexIndex) {
    final int axisCount = this.axisCount;
    if (axisCount > 2) {
      return this.coordinates[vertexIndex * axisCount + 2];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.vertexCount == 0;
  }
}
