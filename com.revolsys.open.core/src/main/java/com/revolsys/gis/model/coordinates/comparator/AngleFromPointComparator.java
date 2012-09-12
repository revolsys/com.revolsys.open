package com.revolsys.gis.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.gis.model.coordinates.Coordinates;

public class AngleFromPointComparator implements Comparator<Coordinates> {

  private final Coordinates point;

  public AngleFromPointComparator(final Coordinates point) {
    this.point = point;
  }

  @Override
  public int compare(final Coordinates c1, final Coordinates c2) {
    final double angleC1 = point.angle2d(c1);
    final double angleC2 = point.angle2d(c2);
    if (angleC1 < angleC2) {
      return 1;
    } else if (angleC1 > angleC2) {
      return -1;
    } else {
      return 0;
    }
  }
}
