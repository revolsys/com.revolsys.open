package com.revolsys.gis.cs.projection;


public class ProjectOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public ProjectOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final int sourceAxisCount,
    final double[] sourceCoordinates, final int targetAxisCount,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      final double x = sourceCoordinates[vertexIndex * sourceAxisCount + 0];
      final double y = sourceCoordinates[vertexIndex * sourceAxisCount + 1];
      projection.project(x, y, targetCoordinates, vertexIndex, targetAxisCount);
      for (int axisIndex = 2; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[vertexIndex * sourceAxisCount + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceAxisCount + axisIndex] = value;
      }
    }
  }
}
