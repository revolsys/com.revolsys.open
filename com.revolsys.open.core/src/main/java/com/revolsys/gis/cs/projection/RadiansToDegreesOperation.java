package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

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
            value = Math.toDegrees(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceNumAxis + axisIndex] = value;
      }
    }
  }
}
