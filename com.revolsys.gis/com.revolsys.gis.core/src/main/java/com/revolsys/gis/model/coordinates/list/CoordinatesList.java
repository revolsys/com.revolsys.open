package com.revolsys.gis.model.coordinates.list;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.PrecisionModel;

public interface CoordinatesList extends CoordinateSequence {
  double[] getCoordinates();

  byte getNumAxis();

  double getValue(
    int index,
    int axisIndex);

  void makePrecise(
    PrecisionModel precisionModel);

  CoordinatesList reverse();

  void setCoordinate(
    int i,
    Coordinate coordinate);

  void setValue(
    int index,
    int axisIndex,
    double value);

  CoordinatesList subList(
    int index,
    int count);

  CoordinatesList subList(
    int length,
    int index,
    int count);

  CoordinatesList subList(
    int length,
    int sourceIndex,
    int targetIndex,
    int count);
}
