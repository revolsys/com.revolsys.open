package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public abstract class AbstractCoordinatesProjection implements
  CoordinatesProjection {
  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = from.getX();
    final double y = from.getY();
    final double[] result = new double[2];
    inverse(x, y, result, 0, 2);
    to.setValue(0, result[0]);
    to.setValue(1, result[1]);
    for (int i = 2; i < from.getAxisCount() && i < to.getAxisCount(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double x = from.getX();
    final double y = from.getY();
    final double[] result = new double[2];
    project(x, y, result, 0, 2);
    to.setValue(0, result[0]);
    to.setValue(1, result[1]);
    for (int i = 2; i < from.getAxisCount() && i < to.getAxisCount(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }
}
