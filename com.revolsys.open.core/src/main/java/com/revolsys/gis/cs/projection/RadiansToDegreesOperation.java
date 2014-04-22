package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class RadiansToDegreesOperation implements CoordinatesOperation {
  public static final RadiansToDegreesOperation INSTANCE = new RadiansToDegreesOperation();

  public RadiansToDegreesOperation() {
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int axisCount = Math.min(from.getAxisCount(), to.getAxisCount());

    for (int i = 0; i < axisCount; i++) {
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
  public void perform(final int sourceAxisCount,
    final double[] sourceCoordinates, final int targetAxisCount,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[vertexIndex * sourceAxisCount + axisIndex];
          if (axisIndex < 2) {
            value = Math.toDegrees(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceAxisCount + axisIndex] = value;
      }
    }
  }
}
