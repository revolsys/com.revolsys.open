package com.revolsys.gis.model.coordinates.list;

import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.util.MathUtil;

public class DoubleCoordinatesList extends AbstractCoordinatesList {
  /**
   * 
   */
  private static final long serialVersionUID = 7579865828939708871L;

  double[] coordinates;

  private final byte axisCount;

  public DoubleCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList.getCoordinates());
  }

  public DoubleCoordinatesList(final int axisCount) {
    this(0, axisCount);
  }

  public DoubleCoordinatesList(final int axisCount,
    final Collection<Coordinates> points) {
    this(points.size(), axisCount);
    int i = 0;
    for (final Coordinates point : points) {
      setPoint(i++, point);
    }
  }

  public DoubleCoordinatesList(final int axisCount, final Coordinates... points) {
    this(points.length, axisCount);
    for (int i = 0; i < points.length; i++) {
      final Coordinates point = points[i];
      setPoint(i, point);
    }
  }

  public DoubleCoordinatesList(final int axisCount,
    final CoordinatesList coordinatesList) {
    this(coordinatesList.size(), axisCount);
    coordinatesList.copy(0, this, 0, axisCount, coordinatesList.size());
  }

  public DoubleCoordinatesList(final int axisCount, final double... coordinates) {
    assert axisCount > 2;
    this.axisCount = (byte)axisCount;
    this.coordinates = coordinates;
  }

  public DoubleCoordinatesList(final int size, final int axisCount) {
    assert axisCount > 2;
    assert size >= 0;
    this.coordinates = new double[size * axisCount];
    this.axisCount = (byte)axisCount;
  }

  public DoubleCoordinatesList(final int axisCount,
    final List<? extends Number> coordinates) {
    this(axisCount, MathUtil.toDoubleArray(coordinates));
  }

  @Override
  public DoubleCoordinatesList clone() {
    return new DoubleCoordinatesList(this);
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  @Override
  public int getAxisCount() {
    return axisCount;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      return coordinates[index * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      coordinates[index * axisCount + axisIndex] = value;
    }
  }

  @Override
  public int size() {
    if (axisCount < 2 || coordinates == null) {
      return 0;
    } else {
      return coordinates.length / axisCount;
    }
  }
}
