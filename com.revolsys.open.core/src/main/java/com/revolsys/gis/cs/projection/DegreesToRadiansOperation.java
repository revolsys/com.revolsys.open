package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class DegreesToRadiansOperation implements CoordinatesOperation {
  public static final DegreesToRadiansOperation INSTANCE = new DegreesToRadiansOperation();

  public DegreesToRadiansOperation() {
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int numAxis = Math.min(from.getNumAxis(), to.getNumAxis());

    for (int i = 0; i < numAxis; i++) {
      final double value = from.getValue(i);
      if (i < 2) {
        final double convertedValue = Math.toRadians(value);
        to.setValue(i, convertedValue);
      } else {
        to.setValue(i, value);
      }
    }

  }
}
