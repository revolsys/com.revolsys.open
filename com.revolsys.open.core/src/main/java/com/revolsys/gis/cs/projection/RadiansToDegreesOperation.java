package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.Coordinates;

public class RadiansToDegreesOperation implements CoordinatesOperation {
  public static final RadiansToDegreesOperation INSTANCE = new RadiansToDegreesOperation();

  public RadiansToDegreesOperation() {
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int numAxis = Math.min(from.getNumAxis(), to.getNumAxis());

    for (int i = 0; i < numAxis; i++) {
      final double value = from.getValue(i);
      if (i < 2) {
        final double convertedValue = Math.toDegrees(value);
        to.setValue(i, convertedValue);
      } else {
        to.setValue(i, value);
      }
    }

  }
}
