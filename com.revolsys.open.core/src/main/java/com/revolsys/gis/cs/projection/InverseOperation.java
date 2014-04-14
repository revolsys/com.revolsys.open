package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class InverseOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public InverseOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    projection.inverse(from, to);
  }

  @Override
  public void perform(final int sourceNumAxis,
    final double[] sourceCoordinates, final int targetNumAxis,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceNumAxis;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      final double x = sourceCoordinates[vertexIndex * sourceNumAxis + 0];
      final double y = sourceCoordinates[vertexIndex * sourceNumAxis + 1];
      projection.inverse(x, y, targetCoordinates, vertexIndex, targetNumAxis);
      for (int axisIndex = 2; axisIndex < targetNumAxis; axisIndex++) {
        double value;
        if (axisIndex < sourceNumAxis) {
          value = sourceCoordinates[vertexIndex * sourceNumAxis + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceNumAxis + axisIndex] = value;
      }
    }
  }
}
