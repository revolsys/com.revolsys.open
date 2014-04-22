package com.revolsys.gis.model.coordinates.list;

import java.io.Serializable;
import java.util.List;

import com.revolsys.jts.geom.CoordinateSequenceFactory;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

public class DoubleCoordinatesListFactory implements CoordinateSequenceFactory,
  Serializable {

  private static final long serialVersionUID = 948765434610427191L;

  public static CoordinatesList create(final List<Coordinates> coordinates) {
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinates.size(), 3);
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinates coordinate = coordinates.get(i);
      coordinatesList.setCoordinate(i, coordinate);
    }
    return coordinatesList;
  }

  @Override
  public CoordinatesList create(final Coordinates[] coordinates) {
    if (coordinates == null) {
      return create(0, 3);
    } else {
      final CoordinatesList coordinatesList = create(coordinates.length, 3);
      for (int i = 0; i < coordinates.length; i++) {
        final Coordinates coordinate = coordinates[i];
        coordinatesList.setCoordinate(i, coordinate);
      }
      return coordinatesList;
    }
  }

  @Override
  public CoordinatesList create(final CoordinatesList coordinateSequence) {
    final int size = coordinateSequence.size();
    final int axisCount = coordinateSequence.getAxisCount();
    final CoordinatesList coordinatesList = create(size, axisCount);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < axisCount; j++) {
        final double coordinate = coordinateSequence.getValue(i, j);
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
