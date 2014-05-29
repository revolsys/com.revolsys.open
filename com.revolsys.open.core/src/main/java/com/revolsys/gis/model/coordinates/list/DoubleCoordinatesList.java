package com.revolsys.gis.model.coordinates.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.util.MathUtil;

public class DoubleCoordinatesList extends AbstractCoordinatesList {
  /**
   * 
   */
  private static final long serialVersionUID = 7579865828939708871L;

  private double[] coordinates;

  private final int axisCount;

  public DoubleCoordinatesList(final int axisCount) {
    this(0, axisCount);
  }

  public DoubleCoordinatesList(final int axisCount,
    final Collection<Point> points) {
    this(points.size(), axisCount);
    int i = 0;
    for (final Point point : points) {
      CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, i++,
        point);
    }
  }

  public DoubleCoordinatesList(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = new double[0];
    } else {
      assert axisCount >= 2;
      this.axisCount = axisCount;
      this.coordinates = coordinates;
    }
  }

  public DoubleCoordinatesList(final int size, final int axisCount) {
    assert axisCount >= 2;
    assert size >= 0;
    this.coordinates = new double[size * axisCount];
    this.axisCount = (byte)axisCount;
  }

  public DoubleCoordinatesList(final int axisCount, final int vertexCount,
    final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = new double[0];
    } else {
      assert axisCount >= 2;
      this.axisCount = (byte)axisCount;
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length="
          + coordinates.length + " must be a multiple of axisCount="
          + axisCount);
      } else if (coordinateCount == coordinates.length) {
        this.coordinates = coordinates;
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount
          + " * vertexCount=" + vertexCount + " > coordinates.length="
          + coordinates.length);
      } else {
        this.coordinates = new double[coordinateCount];
        System.arraycopy(coordinates, 0, this.coordinates, 0, coordinateCount);
      }
    }
  }

  public DoubleCoordinatesList(final int axisCount,
    final List<? extends Number> coordinates) {
    this(axisCount, MathUtil.toDoubleArray(coordinates));
  }

  public DoubleCoordinatesList(final int axisCount, final Point... points) {
    this(axisCount, Arrays.asList(points));
  }

  public DoubleCoordinatesList(final int axisCount, final PointList points) {
    this(points.getVertexCount(), axisCount);
    CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, 0, points,
      0, points.getVertexCount());
  }

  public DoubleCoordinatesList(final Point... coordinates) {
    this(3, coordinates);
  }

  public DoubleCoordinatesList(final PointList coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList);
  }

  @Override
  public DoubleCoordinatesList clone() {
    return new DoubleCoordinatesList(this);
  }

  @Override
  public int getAxisCount() {
    return axisCount;
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      return coordinates[index * axisCount + axisIndex];
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
  public int getVertexCount() {
    if (axisCount < 2 || coordinates == null) {
      return 0;
    } else {
      return coordinates.length / axisCount;
    }
  }
}
