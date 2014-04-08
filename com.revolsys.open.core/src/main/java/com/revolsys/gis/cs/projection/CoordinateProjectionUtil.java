package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.CoordinateCoordinates;
import com.revolsys.jts.geom.Coordinate;

public class CoordinateProjectionUtil {
  public static Coordinate perform(final CoordinatesOperation operation,
    final Coordinate coordinate) {
    if (operation == null) {
      return coordinate;
    } else {
      final Coordinate newCoordinate = new Coordinate();
      perform(operation, coordinate, newCoordinate);
      return newCoordinate;
    }
  }

  public static void perform(final CoordinatesOperation operation,
    final Coordinate from, final Coordinate to) {
    if (operation == null) {
      to.setCoordinate(from);
    } else {
      final CoordinateCoordinates fromCoordinates = new CoordinateCoordinates(
        from);
      final CoordinateCoordinates toCoordinates = new CoordinateCoordinates(to);
      operation.perform(fromCoordinates, toCoordinates);
    }
  }
}
