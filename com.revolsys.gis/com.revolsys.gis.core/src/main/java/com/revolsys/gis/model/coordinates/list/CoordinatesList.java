package com.revolsys.gis.model.coordinates.list;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

public interface CoordinatesList extends CoordinateSequence,
  Iterable<Coordinates> {
  void copy(
    int sourceIndex,
    CoordinatesList target,
    int targetIndex,
    int numAxis,
    int count);

  double[] getCoordinates();

  byte getNumAxis();

  double getValue(
    int index,
    int axisIndex);

  void makePrecise(
    CoordinatesPrecisionModel precisionModel);

  CoordinatesList reverse();

  void setCoordinate(
    int i,
    Coordinate coordinate);

  void setPoint(
    int i,
    Coordinates point);

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

  double getX(
    int index);

  double getY(
    int index);

  double getZ(
    int index);

  void setX(
    int index,
    double x);

  void setY(
    int index,
    double y);

  void setZ(
    int index,
    double z);

  Coordinates getPoint(int i);
}
