package com.revolsys.gis.model.coordinates.list;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

public class DoubleCoordinatesListFactory implements CoordinateSequenceFactory {
  public static final DoubleCoordinatesListFactory INSTANCE = new DoubleCoordinatesListFactory();

  public static CoordinatesList create(
    final List<Coordinate> coordinates) {
    final CoordinatesList coordinatesList = INSTANCE.create(coordinates.size(),
      3);
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinate coordinate = coordinates.get(i);
      coordinatesList.setCoordinate(i, coordinate);
    }
    return coordinatesList;
  }

  public CoordinatesList create(
    final Coordinate[] coordinates) {
    final CoordinatesList coordinatesList = create(coordinates.length, 3);
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      coordinatesList.setCoordinate(i, coordinate);
    }
    return coordinatesList;
  }

  public CoordinatesList create(
    final CoordinateSequence coordinateSequence) {
    final int size = coordinateSequence.size();
    final int numAxis = coordinateSequence.getDimension();
    final CoordinatesList coordinatesList = create(size, numAxis);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = coordinateSequence.getOrdinate(i, i);
        coordinatesList.setValue(i, j, coordinate);
      }
    }
    return coordinatesList;
  }

  public CoordinatesList create(
    final int size,
    final int dimension) {
    return new DoubleCoordinatesList(size, dimension);
  }

}
