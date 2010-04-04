package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.Coordinates;

public class CopyOperation implements CoordinatesOperation {

  public void perform(
    final Coordinates from,
    final Coordinates to) {
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
