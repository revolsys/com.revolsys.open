package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Point;

public class ListCoordinatesList extends AbstractCoordinatesList {

  private static final long serialVersionUID = 0L;

  private List<Coordinates> coordinates = new ArrayList<Coordinates>();

  private final byte axisCount;

  public ListCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList);
  }

  public ListCoordinatesList(final int axisCount) {
    this.axisCount = (byte)axisCount;
  }

  public ListCoordinatesList(final int axisCount,
    final Coordinates... coordinates) {
    this.axisCount = (byte)axisCount;
    for (final Coordinates coordinate : coordinates) {
      add(coordinate);
    }
  }

  public ListCoordinatesList(final int axisCount,
    final CoordinatesList coordinatesList) {
    this(axisCount);
    coordinatesList.copy(0, this, 0, axisCount, coordinatesList.size());
  }

  public ListCoordinatesList(final int axisCount,
    final List<Coordinates> coordinates) {
    this(axisCount);
    for (final Coordinates coordinate : coordinates) {
      add(coordinate);
    }
  }

  public void add(final Coordinates point) {
    coordinates.add(new DoubleCoordinates(point, axisCount));
  }

  public void add(final int index, final Coordinates point) {
    coordinates.add(index, point);
  }

  public void add(final Point point) {
    add(CoordinatesUtil.getInstance(point));
  }

  public void clear() {
    coordinates.clear();
  }

  @Override
  public ListCoordinatesList clone() {
    final ListCoordinatesList clone = (ListCoordinatesList)super.clone();
    clone.coordinates = new ArrayList<Coordinates>(coordinates);
    return clone;
  }

  @Override
  public int getAxisCount() {
    return axisCount;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount && index < size()) {
      return coordinates.get(index).getValue(axisIndex);
    } else {
      return Double.NaN;
    }
  }

  public void remove(final int index) {
    coordinates.remove(index);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      if (index <= size()) {
        for (int i = coordinates.size(); i < (index + 1); i++) {
          add(new DoubleCoordinates(axisCount));
        }
      }
      coordinates.get(index).setValue(axisIndex, value);
    }
  }

  @Override
  public int size() {
    return coordinates.size();
  }
}
