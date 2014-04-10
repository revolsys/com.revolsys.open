package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

public class InPlaceIterator extends AbstractCoordinates implements
  Iterator<Coordinates>, Iterable<Coordinates> {
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

  public int getIndex() {
    return index;
  }

  @Override
  public byte getNumAxis() {
    return coordinates.getNumAxis();
  }

  @Override
  public double getValue(final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      return coordinates.getValue(this.index, axisIndex);
    } else {
      return 0;
    }
  }

  public double getValue(final int relativeIndex, final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
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
  public Iterator<Coordinates> iterator() {
    return this;
  }

  @Override
  public Coordinates next() {
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
  public void setValue(final int axisIndex, final double value) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      coordinates.setValue(this.index, axisIndex, value);
    }
  }

  public void setValue(final int relativeIndex, final int axisIndex,
    final double value) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      coordinates.setValue(this.index + relativeIndex, axisIndex, value);
    }
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    if (numAxis > 0) {
      final StringBuffer s = new StringBuffer(String.valueOf(getValue(0)));
      for (int i = 1; i < numAxis; i++) {
        final Double ordinate = getValue(i);
        s.append(',');
        s.append(ordinate);
      }
      return s.toString();
    } else {
      return "";
    }
  }

}
