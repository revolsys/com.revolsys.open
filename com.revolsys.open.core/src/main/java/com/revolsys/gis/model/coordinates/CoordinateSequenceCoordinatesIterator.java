package com.revolsys.gis.model.coordinates;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

public class CoordinateSequenceCoordinatesIterator extends AbstractCoordinates
  implements Iterator<Coordinates>, Iterable<Coordinates> {
  private final CoordinatesList coordinates;

  private int index = 0;

  public CoordinateSequenceCoordinatesIterator(
    final CoordinatesList coordinates) {
    this.coordinates = coordinates;
  }

  public CoordinateSequenceCoordinatesIterator(
    final CoordinatesList coordinates, final int index) {
    this.coordinates = coordinates;
    this.index = index;
  }

  @Override
  public CoordinateSequenceCoordinatesIterator cloneCoordinates() {
    return new CoordinateSequenceCoordinatesIterator(coordinates, index);
  }

  public int getIndex() {
    return index;
  }

  @Override
  public int getNumAxis() {
    return (byte)coordinates.getNumAxis();
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

  public int size() {
    return coordinates.size();
  }

  @Override
  public String toString() {
    return coordinates.getCoordinate(index).toString();
  }
}
