package com.revolsys.geometry.cs.projection;

public class InverseOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public InverseOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      final int offset = vertexIndex * sourceAxisCount;
      final double x = sourceCoordinates[offset];
      final double y = sourceCoordinates[offset + 1];
      this.projection.inverse(x, y, targetCoordinates, vertexIndex, targetAxisCount);
      for (int axisIndex = 2; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[offset + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * targetAxisCount + axisIndex] = value;
      }
    }
  }

  @Override
  public String toString() {
    return this.projection + " -> geographics";
  }
}
