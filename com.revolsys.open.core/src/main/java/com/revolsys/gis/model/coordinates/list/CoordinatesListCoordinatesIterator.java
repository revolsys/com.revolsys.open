package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Point;

/**
 * The CoordinatesListCoordinatesIterator is an iterator which iterates through
 * each item in a {@link PointList}.
 * 
 * @author Paul Austin
 */
public class CoordinatesListCoordinatesIterator implements Iterator<Point> {
  /** The coordinates list. */
  private final PointList coordinatesList;

  private int index = 0;

  /**
   * Construct a new CoordinatesListCoordinatesIterator.
   * 
   * @param coordinates The coordinates list.
   */
  public CoordinatesListCoordinatesIterator(final PointList coordinates) {
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
  public Point next() {
    if (hasNext()) {
      final int index = this.index;
      this.index++;
      return coordinatesList.get(index);
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
