package com.revolsys.gis.model.coordinates.list;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.util.MathUtil;

public class DoubleCoordinatesList extends AbstractCoordinatesList {

  /**
   * 
   */
  private static final long serialVersionUID = 7579865828939708871L;

  double[] coordinates;

  private final byte numAxis;

  public DoubleCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getNumAxis(), coordinatesList.getCoordinates());
  }

  public DoubleCoordinatesList(final int numAxis) {
    this(0, numAxis);
  }

  public DoubleCoordinatesList(final int numAxis,
    final Collection<Coordinates> points) {
    this(points.size(), numAxis);
    int i = 0;
    for (final Coordinates point : points) {
      setPoint(i++, point);
    }
  }

  public DoubleCoordinatesList(final int numAxis, final Coordinates... points) {
    this(points.length, numAxis);
    for (int i = 0; i < points.length; i++) {
      final Coordinates point = points[i];
      setPoint(i, point);
    }
  }

  public DoubleCoordinatesList(final int numAxis,
    final CoordinatesList coordinatesList) {
    this(coordinatesList.size(), numAxis);
    coordinatesList.copy(0, this, 0, numAxis, coordinatesList.size());
  }

  public DoubleCoordinatesList(final int numAxis, final double... coordinates) {
    this.numAxis = (byte)numAxis;
    this.coordinates = coordinates;
  }

  public DoubleCoordinatesList(final int size, final int numAxis) {
    this.coordinates = new double[size * numAxis];
    this.numAxis = (byte)numAxis;
  }

  public DoubleCoordinatesList(final int numAxis,
    final List<? extends Number> coordinates) {
    this(numAxis, MathUtil.toDoubleArray(coordinates));
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
  public byte getNumAxis() {
    return numAxis;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis) {
      return coordinates[index * numAxis + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis) {
      coordinates[index * numAxis + axisIndex] = value;
    }
  }

  @Override
  public int size() {
    return coordinates.length / numAxis;
  }
}
