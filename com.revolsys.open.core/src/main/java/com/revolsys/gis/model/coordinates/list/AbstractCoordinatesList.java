package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractCoordinatesList implements CoordinatesList,
  Cloneable {

  /**
   * 
   */
  private static final long serialVersionUID = 9211011581013036939L;

  private void append(final StringBuffer s, final int i, final byte numAxis) {
    s.append(getX(i));
    s.append(' ');
    s.append(getY(i));
    for (int j = 2; j < numAxis; j++) {
      final Double coordinate = getValue(i, j);
      s.append(' ');
      s.append(coordinate);
    }
  }

  @Override
  public AbstractCoordinatesList clone() {
    try {
      return (AbstractCoordinatesList)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean contains(final Coordinates point) {
    for (int i = 0; i < size(); i++) {
      if (equal(i, point, 2)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void copy(final int sourceIndex, final CoordinatesList target,
    final int targetIndex, final int numAxis, final int count) {
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = getValue(sourceIndex + i, j);
        target.setValue(targetIndex + i, j, coordinate);
      }
    }
  }

  public CoordinatesList create(final int length, final int numAxis) {
    return new DoubleCoordinatesList(length, numAxis);
  }

  @Override
  public double distance(final int index, final Coordinates point) {
    if (index < size()) {
      final double x1 = getX(index);
      final double y1 = getY(index);
      final double x2 = point.getX();
      final double y2 = point.getY();
      return MathUtil.distance(x1, y1, x2, y2);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double distance(final int index, final CoordinatesList other,
    final int otherIndex) {
    if (index < size() || otherIndex < other.size()) {
      final double x1 = getX(index);
      final double y1 = getY(index);
      final double x2 = other.getX(otherIndex);
      final double y2 = other.getY(otherIndex);
      return MathUtil.distance(x1, y1, x2, y2);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public boolean equal(final int index, final Coordinates point) {
    final int numAxis = Math.max(getNumAxis(), point.getNumAxis());
    return equal(index, point, numAxis);
  }

  @Override
  public boolean equal(final int index, final Coordinates point,
    final int numAxis) {
    int maxAxis = Math.max(getNumAxis(), point.getNumAxis());
    if (maxAxis > numAxis) {
      maxAxis = numAxis;
    }
    if (getNumAxis() < maxAxis) {
      return false;
    } else if (point.getNumAxis() < maxAxis) {
      return false;
    } else if (index < size()) {
      for (int j = 0; j < maxAxis; j++) {
        final double value1 = getValue(index, j);
        final double value2 = point.getValue(j);
        if (Double.compare(value1, value2) != 0) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean equal(final int index, final CoordinatesList other,
    final int otherIndex) {
    final int numAxis = Math.max(getNumAxis(), other.getNumAxis());
    if (index < size() || otherIndex < other.size()) {
      for (int j = 0; j < numAxis; j++) {
        final double value1 = getValue(index, j);
        final double value2 = other.getValue(otherIndex, j);
        if (Double.compare(value1, value2) != 0) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean equal(final int index, final CoordinatesList other,
    final int otherIndex, int numAxis) {
    numAxis = Math.min(numAxis, Math.max(getNumAxis(), other.getNumAxis()));
    if (index < size() || otherIndex < other.size()) {
      for (int j = 0; j < numAxis; j++) {
        final double value1 = getValue(index, j);
        final double value2 = other.getValue(otherIndex, j);
        if (Double.compare(value1, value2) != 0) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean equal2d(final int index, final Coordinates point) {
    return equal(index, point, 2);
  }

  @Override
  public boolean equals(final CoordinatesList coordinatesList) {
    if (getNumAxis() == coordinatesList.getNumAxis()) {
      if (size() == coordinatesList.size()) {
        for (int i = 0; i < size(); i++) {
          for (int j = 0; j < getNumAxis(); j++) {
            final double value1 = getValue(i, j);
            final double value2 = coordinatesList.getValue(i, j);
            if (Double.compare(value1, value2) != 0) {
              return false;
            }
          }
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean equals(final CoordinatesList points, final int numAxis) {
    double maxAxis = Math.max(getNumAxis(), points.getNumAxis());
    if (maxAxis > numAxis) {
      maxAxis = numAxis;
    }
    if (getNumAxis() < maxAxis) {
      return false;
    } else if (points.getNumAxis() < maxAxis) {
      return false;
    } else if (size() == points.size()) {
      for (int i = 0; i < size(); i++) {
        for (int j = 0; j < numAxis; j++) {
          final double value1 = getValue(i, j);
          final double value2 = points.getValue(i, j);
          if (Double.compare(value1, value2) != 0) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof CoordinatesList) {
      final CoordinatesList points = (CoordinatesList)object;
      return equals(points);
    } else {
      return false;
    }
  }

  @Override
  public Envelope expandEnvelope(final Envelope env) {
    for (int i = 0; i < size(); i++) {
      final double x = getX(i);
      final double y = getY(i);
      env.expandToInclude(x, y);
    }
    return env;
  }

  @Override
  public Coordinates get(final int i) {
    return new CoordinatesListCoordinates(this, i);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final BoundingBox boundingBox = new BoundingBox();
    for (final Coordinates point : this) {
      boundingBox.expandToInclude(point);
    }
    return boundingBox;
  }

  @Override
  public Coordinate getCoordinate(final int i) {
    final Coordinate coordinate = new Coordinate();
    getCoordinate(i, coordinate);
    return coordinate;
  }

  @Override
  public void getCoordinate(final int index, final Coordinate coord) {
    coord.x = getX(index);
    coord.y = getY(index);
    if (getNumAxis() > 2) {
      coord.z = getZ(index);
    }
  }

  @Override
  public Coordinate getCoordinateCopy(final int i) {
    final Coordinate coordinate = new Coordinate();
    getCoordinate(i, coordinate);
    return coordinate;
  }

  @Override
  public double[] getCoordinates() {
    final int size = size();
    final byte numAxis = getNumAxis();
    final double[] coordinates = new double[size * numAxis];
    int k = 0;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = getValue(i, j);
        coordinates[k] = coordinate;
        k++;
      }
    }
    return coordinates;
  }

  @Override
  @Deprecated
  public int getDimension() {
    return getNumAxis();
  }

  @Override
  public List<Coordinates> getList() {
    final List<Coordinates> points = new ArrayList<Coordinates>();
    for (final Coordinates point : this) {
      points.add(point);
    }
    return points;
  }

  @Override
  public double getM(final int index) {
    return getValue(index, 3);
  }

  @Override
  @Deprecated
  public double getOrdinate(final int index, final int axisIndex) {
    return getValue(index, axisIndex);
  }

  @Override
  public long getTime(final int index) {
    return (long)getM(index);
  }

  @Override
  public double getX(final int index) {
    return getValue(index, 0);
  }

  @Override
  public double getY(final int index) {
    return getValue(index, 1);
  }

  @Override
  public double getZ(final int index) {
    return getValue(index, 2);
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (int i = 0; i < size(); i++) {
      for (int j = 0; j < getNumAxis(); j++) {
        h = 31 * h + ((Double)getValue(i, j)).hashCode();
      }
    }
    return h;
  }

  @Override
  public Iterator<Coordinates> iterator() {
    return new CoordinatesListCoordinatesIterator(this);
  }

  @Override
  public void makePrecise(final CoordinatesPrecisionModel precisionModel) {
    final InPlaceIterator iterator = new InPlaceIterator(this);
    for (final Coordinates point : iterator) {
      precisionModel.makePrecise(point);
    }
  }

  @Override
  public CoordinatesList reverse() {
    return new ReverseCoordinatesList(this);
  }

  @Override
  public void setCoordinate(final int i, final Coordinate coordinate) {
    setValue(i, 0, coordinate.x);
    setValue(i, 1, coordinate.y);
    if (getNumAxis() > 2) {
      setValue(i, 2, coordinate.z);
    }
  }

  @Override
  public void setM(final int index, final double m) {
    setValue(index, 3, m);
  }

  @Override
  @Deprecated
  public void setOrdinate(final int index, final int axisIndex,
    final double value) {
    setValue(index, axisIndex, value);
  }

  @Override
  public void setPoint(final int i, final Coordinates point) {
    setX(i, point.getX());
    setY(i, point.getY());
    if (getNumAxis() > 2) {
      setZ(i, point.getZ());
    }
  }

  @Override
  public void setTime(final int index, final long time) {
    setM(index, time);
  }

  @Override
  public void setX(final int index, final double x) {
    setValue(index, 0, x);
  }

  @Override
  public void setY(final int index, final double y) {
    setValue(index, 1, y);
  }

  @Override
  public void setZ(final int index, final double z) {
    setValue(index, 2, z);
  }

  @Override
  public boolean startsWith(final CoordinatesList coordinatesList,
    final int numAxis) {
    if (size() > 1 && coordinatesList.size() > 1) {
      if (getNumAxis() >= numAxis && coordinatesList.getNumAxis() >= numAxis) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < numAxis; j++) {
            final double value1 = getValue(i, j);
            final double value2 = coordinatesList.getValue(i, j);
            if (Double.compare(value1, value2) != 0) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public CoordinatesList subList(final int index) {
    return subList(index, size() - index);
  }

  @Override
  public CoordinatesList subList(final int index, final int count) {
    return subList(count, index, count);
  }

  @Override
  public CoordinatesList subList(final int length, final int index,
    final int count) {
    return subList(length, index, 0, count);
  }

  @Override
  public CoordinatesList subList(final int length, final int sourceIndex,
    final int targetIndex, final int count) {
    final int numAxis = getNumAxis();
    final CoordinatesList target = create(length, numAxis);
    copy(sourceIndex, target, targetIndex, numAxis, count);
    return target;
  }

  @Override
  public Coordinate[] toCoordinateArray() {
    final Coordinate[] coordinateArray = new Coordinate[size()];
    for (int i = 0; i < coordinateArray.length; i++) {
      coordinateArray[i] = getCoordinateCopy(i);
    }
    return coordinateArray;
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    if (numAxis > 0 && size() > 0) {
      final StringBuffer s = new StringBuffer("LINESTRING(");
      append(s, 0, numAxis);
      for (int i = 1; i < size(); i++) {
        s.append(',');
        append(s, i, numAxis);
      }
      s.append(')');
      return s.toString();
    } else {
      return "LINESTRING EMPTY";
    }
  }
}
