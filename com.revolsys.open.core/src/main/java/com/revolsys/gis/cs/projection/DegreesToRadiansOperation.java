package com.revolsys.gis.cs.projection;


public class DegreesToRadiansOperation implements CoordinatesOperation {
  public static final DegreesToRadiansOperation INSTANCE = new DegreesToRadiansOperation();

  public DegreesToRadiansOperation() {
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
            value = Math.toRadians(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * targetAxisCount + axisIndex] = value;
      }
    }
  }
}
