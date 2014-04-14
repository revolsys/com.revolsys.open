package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class CopyOperation implements CoordinatesOperation {

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int numAxis = Math.min(from.getNumAxis(), to.getNumAxis());
    for (int i = 0; i < numAxis; i++) {
      final double value = from.getValue(i);
      to.setValue(i, value);
    }
  }

  @Override
  public void perform(final int sourceNumAxis,
    final double[] sourceCoordinates, final int targetNumAxis,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceNumAxis;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetNumAxis; axisIndex++) {
        final double value;
        if (axisIndex < sourceNumAxis) {
          value = sourceCoordinates[vertexIndex * sourceNumAxis + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceNumAxis + axisIndex] = value;
      }
    }
  }

  @Override
  public String toString() {
    return "copy";
  }
}
