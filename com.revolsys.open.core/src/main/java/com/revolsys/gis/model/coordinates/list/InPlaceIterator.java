package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.impl.AbstractPoint;

public class InPlaceIterator extends AbstractPoint implements
  Iterator<Point>, Iterable<Point> {
  private final CoordinatesList coordinates;

  private int index = -1;

  public InPlaceIterator(final CoordinatesList coordinates) {
    this.coordinates = coordinates;
  }

  public InPlaceIterator(final CoordinatesList coordinates, final int index) {
    this.coordinates = coordinates;
    this.index = index;
  }

  @Override
  public InPlaceIterator cloneCoordinates() {
    return new InPlaceIterator(coordinates, index);
  }

  @Override
  public int getAxisCount() {
    return coordinates.getAxisCount();
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getAxisCount()) {
      return coordinates.getValue(this.index, axisIndex);
    } else {
      return 0;
    }
  }

  public int getIndex() {
    return index;
  }

  public double getValue(final int relativeIndex, final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getAxisCount()) {
      return coordinates.getValue(this.index + relativeIndex, axisIndex);
    } else {
      return 0;
    }
  }

  @Override
  public boolean hasNext() {
    return index < coordinates.size() - 1;
  }

  @Override
  public Iterator<Point> iterator() {
    return this;
  }

  @Override
  public Point next() {
    if (hasNext()) {
      index++;
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();

  }

  public void setIndex(final int index) {
    this.index = index;
  }

  @Override
  public String toString() {
    final int axisCount = getAxisCount();
    if (axisCount > 0) {
      final StringBuffer s = new StringBuffer(String.valueOf(getCoordinate(0)));
      for (int i = 1; i < axisCount; i++) {
        final Double ordinate = getCoordinate(i);
        s.append(',');
        s.append(ordinate);
      }
      return s.toString();
    } else {
      return "";
    }
  }

}
