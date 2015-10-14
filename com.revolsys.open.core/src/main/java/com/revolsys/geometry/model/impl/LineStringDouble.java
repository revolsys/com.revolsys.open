package com.revolsys.geometry.model.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;

public class LineStringDouble extends AbstractLineString {
  private static final long serialVersionUID = 7579865828939708871L;

  private final int axisCount;

  private double[] coordinates;

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
      this.coordinates = null;
    } else {
      assert axisCount >= 2;
      this.axisCount = axisCount;
      this.coordinates = coordinates;
    }
  }

  protected LineStringDouble(final int size, final int axisCount) {
    assert axisCount >= 2;
    assert size >= 0;
    this.coordinates = new double[size * axisCount];
    this.axisCount = (byte)axisCount;
  }

  public LineStringDouble(final int axisCount, final int vertexCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = null;
    } else {
      assert axisCount >= 2;
      this.axisCount = (byte)axisCount;
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length=" + coordinates.length
          + " must be a multiple of axisCount=" + axisCount);
      } else if (coordinateCount == coordinates.length) {
        this.coordinates = coordinates;
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
          + vertexCount + " > coordinates.length=" + coordinates.length);
      } else {
        this.coordinates = new double[coordinateCount];
        System.arraycopy(coordinates, 0, this.coordinates, 0, coordinateCount);
      }
    }
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
  public int getAxisCount() {
    return this.axisCount;
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      return this.coordinates[index * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating(0, this.axisCount);
  }

  @Override
  public int getVertexCount() {
    if (this.axisCount < 2 || this.coordinates == null) {
      return 0;
    } else {
      return this.coordinates.length / this.axisCount;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.coordinates == null;
  }
}
