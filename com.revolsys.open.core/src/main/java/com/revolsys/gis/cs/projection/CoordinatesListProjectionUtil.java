package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;

public class CoordinatesListProjectionUtil {

  public static CoordinatesList perform(final CoordinatesList coordinates,
    final CoordinatesOperation operation) {
    final int dimension = coordinates.getDimension();
    final int size = coordinates.size();
    final CoordinatesList newCoordinates = new DoubleCoordinatesList(size,
      dimension);
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
