package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.jts.geom.CoordinatesList;

/**
 * The CoordinatesListCoordinatesIterator is an iterator which iterates through
 * each item in a {@link CoordinatesList}.
 * 
 * @author Paul Austin
 */
public class CoordinatesListCoordinatesIterator implements
  Iterator<Coordinates> {
  /** The coordinates list. */
  private final CoordinatesList coordinatesList;

  private int index = 0;

  /**
   * Construct a new CoordinatesListCoordinatesIterator.
   * 
   * @param coordinates The coordinates list.
   */
  public CoordinatesListCoordinatesIterator(final CoordinatesList coordinates) {
    this.coordinatesList = coordinates;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public boolean hasNext() {
    return index < coordinatesList.size();
  }

  @Override
  public Coordinates next() {
    if (hasNext()) {
      final CoordinatesListCoordinates coordinates = new CoordinatesListCoordinates(
        coordinatesList, index);
      index++;
      return coordinates;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();

  }

  @Override
  public String toString() {
    return coordinatesList.toString();
  }
}
