package com.revolsys.gis.model.coordinates;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateSequenceCoordinatesIterator implements Coordinates,
  Iterator<Coordinates>, Iterable<Coordinates> {
  private final CoordinateSequence coordinates;

  private int index = 0;

  public CoordinateSequenceCoordinatesIterator(
    final CoordinateSequence coordinates) {
    this.coordinates = coordinates;
  }

  public int getIndex() {
    return index;
  }

  public byte getNumAxis() {
    return (byte)coordinates.getDimension();
  }

  public double getValue(
    final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      return coordinates.getOrdinate(this.index, axisIndex);
    } else {
      return 0;
    }
  }

  public double getValue(
    final int relativeIndex,
    final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      return coordinates.getOrdinate(this.index + relativeIndex, axisIndex);
    } else {
      return 0;
    }
  }

  public boolean hasNext() {
    return index < coordinates.size() - 1;
  }

  public Iterator<Coordinates> iterator() {
    return this;
  }

  public Coordinates next() {
    if (hasNext()) {
      index++;
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();

  }

  public void setIndex(
    final int index) {
    this.index = index;
  }

  public void setValue(
    final int axisIndex,
    final double value) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      coordinates.setOrdinate(this.index, axisIndex, value);
    }
  }

  public void setValue(
    final int relativeIndex,
    final int axisIndex,
    final double value) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      coordinates.setOrdinate(this.index + relativeIndex, axisIndex, value);
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
