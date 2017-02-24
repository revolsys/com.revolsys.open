package com.revolsys.geometry.cs.projection;

public class ProjectOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public ProjectOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final CoordinatesProjection projection = this.projection;
    final int sourceLength = sourceCoordinates.length;
    int targetOffset = 0;
    for (int sourceOffset = 0; sourceOffset < sourceLength; sourceOffset += sourceAxisCount) {
      final double x = sourceCoordinates[sourceOffset];
      final double y = sourceCoordinates[sourceOffset + 1];
      projection.project(x, y, targetCoordinates, targetOffset);
      for (int axisIndex = 2; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[sourceOffset + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[targetOffset + axisIndex] = value;
      }
      targetOffset += targetAxisCount;
    }
  }

  @Override
  public String toString() {
    return "geographics -> " + this.projection;
  }
}
