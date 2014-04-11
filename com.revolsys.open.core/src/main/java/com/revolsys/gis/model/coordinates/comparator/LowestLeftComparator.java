package com.revolsys.gis.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.jts.geom.Coordinates;

/**
 * Compare the coordinates, lowest Y first if equal then X comparison.
 */
public class LowestLeftComparator implements Comparator<Coordinates> {
  public static int compareCoordinates(final Coordinates point1,
    final Coordinates point2) {
    final Double x1 = point1.getX();
    final Double y1 = point1.getY();
    final Double x2 = point2.getX();
    final Double y2 = point2.getY();

    final int yCompare = y1.compareTo(y2);
    if (yCompare == 0) {
      return x1.compareTo(x2);
    } else {
      return yCompare;
    }
  }

  public LowestLeftComparator() {
  }

  @Override
  public int compare(final Coordinates point1, final Coordinates point2) {
    return compareCoordinates(point1, point2);
  }
}
