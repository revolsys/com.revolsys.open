package org.jeometry.coordinatesystem.operation;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.function.BiConsumerDouble;

public interface CoordinatesOperation {

  default void perform(final CoordinatesOperationPoint point) {
    final double[] coordinates = new double[] {
      point.x, point.y, point.z, point.m
    };
    perform(4, coordinates, 4, coordinates);
    point.x = coordinates[0];
    point.y = coordinates[1];
    point.z = coordinates[2];
    point.m = coordinates[3];
  }

  void perform(int sourceAxisCount, double[] sourceCoordinates, int targetAxisCount,
    double[] targetCoordinates);

  default void perform2d(final CoordinatesOperationPoint point, final double x, final double y,
    final BiConsumerDouble action) {
    final double[] coordinates = {
      x, y
    };
    perform(2, coordinates, 2, coordinates);
    point.setPoint(coordinates, 0, 2);
    point.apply2d(action);
  }

  default void perform2d(final Geometry geometry, final BiConsumerDouble action) {
    final double[] coordinates = new double[2];

    for (final Vertex vertex : geometry.vertices()) {
      coordinates[0] = vertex.getX();
      coordinates[1] = vertex.getY();
      perform(2, coordinates, 2, coordinates);
      action.accept(coordinates[0], coordinates[1]);
    }
  }
}
