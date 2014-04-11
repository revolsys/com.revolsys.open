package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;

public class CoordinateProjectionUtil {
  public static Coordinates perform(final CoordinatesOperation operation,
    final Coordinates coordinate) {
    if (operation == null) {
      return coordinate;
    } else {
      final Coordinates newCoordinate = new DoubleCoordinates(
        coordinate.getNumAxis());
      perform(operation, coordinate, newCoordinate);
      return newCoordinate;
    }
  }

  public static void perform(final CoordinatesOperation operation,
    final Coordinates from, final Coordinates to) {
    if (operation == null) {
      to.setCoordinate(from);
    } else {
      operation.perform(from, to);
    }
  }
}
