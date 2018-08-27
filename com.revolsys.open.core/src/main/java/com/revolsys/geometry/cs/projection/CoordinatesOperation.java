package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.util.function.BiConsumerDouble;

public interface CoordinatesOperation {

  void perform(CoordinatesOperationPoint point);

  default void perform2d(final CoordinatesOperationPoint point, final double x, final double y,
    final BiConsumerDouble action) {
    point.setPoint(x, y);
    perform(point);
    point.apply2d(action);
  }

  default void perform2d(final Geometry geometry, final BiConsumerDouble action) {
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
    geometry.forEachVertex((x, y) -> {
      point.setPoint(x, y);
      perform(point);
      point.apply2d(action);
    });
  }
}
