package com.revolsys.gis.model.coordinates.list;

import java.io.Serializable;
import java.util.List;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateSequence;
import com.revolsys.jts.geom.CoordinateSequenceFactory;

public class DoubleCoordinatesListFactory implements CoordinateSequenceFactory,
  Serializable {

  private static final long serialVersionUID = 948765434610427191L;

  public static CoordinatesList create(final List<Coordinate> coordinates) {
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinates.size(), 3);
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinate coordinate = coordinates.get(i);
      coordinatesList.setCoordinate(i, coordinate);
    }
    return coordinatesList;
  }

  @Override
  public CoordinatesList create(final Coordinate[] coordinates) {
    final CoordinatesList coordinatesList = create(coordinates.length, 3);
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      coordinatesList.setCoordinate(i, coordinate);
    }
    return coordinatesList;
  }

  @Override
  public CoordinatesList create(final CoordinateSequence coordinateSequence) {
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

  @Override
  public CoordinatesList create(final int size, final int dimension) {
    return new DoubleCoordinatesList(size, dimension);
  }

}
