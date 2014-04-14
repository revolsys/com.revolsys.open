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

  @Override
  public void perform(final int sourceNumAxis,
    final double[] sourceCoordinates, final int targetNumAxis,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceNumAxis;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetNumAxis; axisIndex++) {
        double value;
        if (axisIndex < sourceNumAxis) {
          value = sourceCoordinates[vertexIndex * sourceNumAxis + axisIndex];
          if (axisIndex < 2) {
            value = Math.toRadians(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceNumAxis + axisIndex] = value;
      }
    }
  }
}
