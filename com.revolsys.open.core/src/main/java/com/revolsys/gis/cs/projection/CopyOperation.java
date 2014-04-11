package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class CopyOperation implements CoordinatesOperation {

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int dimension = Math.min(from.getNumAxis(), to.getNumAxis());
    for (int i = 0; i < dimension; i++) {
      final double value = from.getValue(i);
      to.setValue(i, value);
    }
  }

  @Override
  public String toString() {
    return "copy";
  }
}
