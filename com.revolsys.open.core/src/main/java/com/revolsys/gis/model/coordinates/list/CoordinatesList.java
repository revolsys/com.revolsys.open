package com.revolsys.gis.model.coordinates.list;

import java.io.Serializable;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateSequence;

public interface CoordinatesList extends CoordinateSequence,
  Iterable<Coordinates>, Serializable {
  @Override
  CoordinatesList clone();

  boolean contains(Coordinates point);

  void copy(int sourceIndex, CoordinatesList target, int targetIndex,
    int numAxis, int count);

  double distance(int index, Coordinates point);

  double distance(int index, CoordinatesList other, int otherIndex);

  boolean equal(int i, Coordinates point);

  boolean equal(int i, Coordinates point, int numAxis);

  boolean equal(int index, CoordinatesList other, int otherIndex);

  boolean equal(int index, CoordinatesList other, int otherIndex, int numAxis);

  boolean equal2d(int index, Coordinates point);

  boolean equals(CoordinatesList coordinatesList);

  boolean equals(CoordinatesList coordinatesList, int numAxis);

  Coordinates get(int i);

  double[] getCoordinates();

  List<Coordinates> getList();

  double getM(int index);

  byte getNumAxis();

  long getTime(int index);

  double getValue(int index, int axisIndex);

  @Override
  double getX(int index);

  @Override
  double getY(int index);

  double getZ(int index);

  void makePrecise(CoordinatesPrecisionModel precisionModel);

  CoordinatesList reverse();

  void setCoordinate(int i, Coordinate coordinate);

  void setM(int index, double m);

  void setPoint(int i, Coordinates point);

  void setTime(int index, long time);

  void setValue(int index, int axisIndex, double value);

  void setX(int index, double x);

  void setY(int index, double y);

  void setZ(int index, double z);

  @Override
  int size();

  boolean startsWith(CoordinatesList coordinatesList, int numAxis);

  CoordinatesList subList(int index);

  CoordinatesList subList(int index, int count);

  CoordinatesList subList(int length, int index, int count);

  CoordinatesList subList(int length, int sourceIndex, int targetIndex,
    int count);
}
