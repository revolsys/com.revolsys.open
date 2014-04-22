package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.CoordinatesList;

public class CoordinatesListProjectionUtil {

  public static CoordinatesList perform(final CoordinatesList coordinates,
    final CoordinatesOperation operation) {
    final int axisCount = coordinates.getAxisCount();
    final int size = coordinates.size();
    final CoordinatesList newCoordinates = new DoubleCoordinatesList(size,
      axisCount);
    final CoordinatesListCoordinates sourceCoordinates = new CoordinatesListCoordinates(
      coordinates);
    final CoordinatesListCoordinates targetCoordinates = new CoordinatesListCoordinates(
      newCoordinates);
    for (int i = 0; i < size; i++) {
      sourceCoordinates.setIndex(i);
      targetCoordinates.setIndex(i);
      operation.perform(sourceCoordinates, targetCoordinates);
    }
    return newCoordinates;
  }

  public static CoordinatesList perform(final CoordinatesList coordinates,
    final CoordinateSystem fromCoordinateSystem,
    final CoordinateSystem toCoordinateSystem) {
    final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
      fromCoordinateSystem, toCoordinateSystem);
    if (operation == null) {
      return coordinates;
    } else {
      return perform(coordinates, operation);
    }
  }

}
