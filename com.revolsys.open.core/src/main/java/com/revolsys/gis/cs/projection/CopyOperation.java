package com.revolsys.gis.cs.projection;

public class CopyOperation implements CoordinatesOperation {

  @Override
  public void perform(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetAxisCount; axisIndex++) {
        final double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[vertexIndex * sourceAxisCount + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceAxisCount + axisIndex] = value;
      }
    }
  }

  @Override
  public String toString() {
    return "copy";
  }
}
