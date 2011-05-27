package com.revolsys.gis.model.coordinates.list;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

public interface CoordinatesList extends CoordinateSequence,
  Iterable<Coordinates> {
  void copy(int sourceIndex, CoordinatesList target, int targetIndex,
    int numAxis, int count);

  double[] getCoordinates();

  byte getNumAxis();

  double getValue(int index, int axisIndex);

  void makePrecise(CoordinatesPrecisionModel precisionModel);

  boolean equals(CoordinatesList coordinatesList);

  CoordinatesList reverse();

  void setCoordinate(int i, Coordinate coordinate);

  boolean startsWith(CoordinatesList coordinatesList, int numAxis);

  void setPoint(int i, Coordinates point);

  void setValue(int index, int axisIndex, double value);

  CoordinatesList subList(int index, int count);

  CoordinatesList subList(int length, int index, int count);

  CoordinatesList subList(int length, int sourceIndex, int targetIndex,
    int count);

  boolean equal(int index, CoordinatesList other, int otherIndex);

  boolean equal(int index, CoordinatesList other, int otherIndex, int numAxis);

  double distance(int index, CoordinatesList other, int otherIndex);

  double getX(int index);

  double getY(int index);

  double getZ(int index);

  double getM(int index);

  long getTime(int index);

  void setX(int index, double x);

  void setY(int index, double y);

  void setZ(int index, double z);

  void setTime(int index, long time);

  void setM(int index, double m);

  Coordinates get(int i);

  int size();
}
