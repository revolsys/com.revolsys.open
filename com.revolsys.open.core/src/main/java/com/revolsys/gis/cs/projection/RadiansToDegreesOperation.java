package com.revolsys.gis.cs.projection;


public class RadiansToDegreesOperation implements CoordinatesOperation {
  public static final RadiansToDegreesOperation INSTANCE = new RadiansToDegreesOperation();

  public RadiansToDegreesOperation() {
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
