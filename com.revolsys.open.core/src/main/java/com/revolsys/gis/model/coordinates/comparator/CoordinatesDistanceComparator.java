package com.revolsys.gis.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.gis.model.coordinates.Coordinates;

public class CoordinatesDistanceComparator implements Comparator<Coordinates> {

  private final boolean invert;

  private final Coordinates point;

  public CoordinatesDistanceComparator(final Coordinates point) {
    this.point = point;
    this.invert = false;
  }

  public CoordinatesDistanceComparator(final Coordinates point,
    final boolean invert) {
    this.point = point;
    this.invert = invert;
  }

  public int compare(final Coordinates point1, final Coordinates point2) {
    int compare;
    final double distance1 = point1.distance(point);
    final double distance2 = point2.distance(point);
    if (distance1 == distance2) {
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (invert) {
      return -compare;
    } else {
      return compare;
    }
  }

  public boolean isInvert() {
    return invert;
  }

}
