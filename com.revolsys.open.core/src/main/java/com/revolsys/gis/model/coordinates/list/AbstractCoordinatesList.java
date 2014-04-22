package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.util.MathUtil;

public abstract class AbstractCoordinatesList implements CoordinatesList,
  Cloneable {

  /**
   * 
   */
  private static final long serialVersionUID = 9211011581013036939L;

  public void append(final StringBuffer s, final int i, final int axisCount) {
    s.append(getX(i));
    s.append(' ');
    s.append(getY(i));
    for (int j = 2; j < axisCount; j++) {
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
    final int targetIndex, final int axisCount, final int count) {
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < axisCount; j++) {
        final double coordinate = getValue(sourceIndex + i, j);
        target.setValue(targetIndex + i, j, coordinate);
      }
    }
  }

  public CoordinatesList create(final int length, final int axisCount) {
    return new DoubleCoordinatesList(length, axisCount);
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
    final int axisCount = Math.max(getAxisCount(), point.getAxisCount());
    return equal(index, point, axisCount);
  }

  @Override
  public boolean equal(final int index, final Coordinates point,
    final int axisCount) {
    int maxAxis = Math.max(getAxisCount(), point.getAxisCount());
    if (maxAxis > axisCount) {
      maxAxis = axisCount;
    }
    if (getAxisCount() < maxAxis) {
      return false;
    } else if (point.getAxisCount() < maxAxis) {
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
    final int axisCount = Math.max(getAxisCount(), other.getAxisCount());
    if (index < size() || otherIndex < other.size()) {
      for (int j = 0; j < axisCount; j++) {
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
    final int otherIndex, int axisCount) {
    axisCount = Math.min(axisCount, Math.max(getAxisCount(), other.getAxisCount()));
    if (index < size() || otherIndex < other.size()) {
      for (int j = 0; j < axisCount; j++) {
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
    if (getAxisCount() == coordinatesList.getAxisCount()) {
      if (size() == coordinatesList.size()) {
        for (int i = 0; i < size(); i++) {
          for (int j = 0; j < getAxisCount(); j++) {
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
  public boolean equals(final CoordinatesList points, final int axisCount) {
    double maxAxis = Math.max(getAxisCount(), points.getAxisCount());
    if (maxAxis > axisCount) {
      maxAxis = axisCount;
    }
    if (getAxisCount() < maxAxis) {
      return false;
    } else if (points.getAxisCount() < maxAxis) {
      return false;
    } else if (size() == points.size()) {
      for (int i = 0; i < size(); i++) {
        for (int j = 0; j < axisCount; j++) {
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
  public Coordinates get(final int i) {
    if (i >= 0 && i < size()) {
      return new CoordinatesListCoordinates(this, i);
    } else {
      return null;
    }
  }

  @Override
  public Coordinates getCoordinate(final int i) {
    final Coordinates coordinate = new DoubleCoordinates(getAxisCount());
    getCoordinate(i, coordinate);
    return coordinate;
  }

  @Override
  public void getCoordinate(final int index, final Coordinates coord) {
    coord.setX(getX(index));
    coord.setY(getY(index));
    if (getAxisCount() > 2) {
      coord.setZ(getZ(index));
    }
  }

  @Override
  public Coordinates getCoordinateCopy(final int i) {
    return getCoordinate(i);
  }

  @Override
  public double[] getCoordinates() {
    final int size = size();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[size * axisCount];
    int k = 0;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < axisCount; j++) {
        final double coordinate = getValue(i, j);
        coordinates[k] = coordinate;
        k++;
      }
    }
    return coordinates;
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
  public int getAxisCount() {
    return getAxisCount();
  }

  @Override
  public long getTime(final int index) {
    return (long)getM(index);
  }

  @Override
  @Deprecated
  public double getValue(final int index, final int axisIndex) {
    return getValue(index, axisIndex);
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
      for (int j = 0; j < getAxisCount(); j++) {
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
  public void setCoordinate(final int i, final Coordinates coordinate) {
    setValue(i, 0, coordinate.getX());
    setValue(i, 1, coordinate.getY());
    if (getAxisCount() > 2) {
      setValue(i, 2, coordinate.getZ());
    }
  }

  @Override
  public void setM(final int index, final double m) {
    setValue(index, 3, m);
  }

  @Override
  public void setPoint(final int i, final Coordinates point) {
    setX(i, point.getX());
    setY(i, point.getY());
    if (getAxisCount() > 2) {
      setZ(i, point.getZ());
    }
  }

  @Override
  public void setTime(final int index, final long time) {
    setM(index, time);
  }

  @Override
  @Deprecated
  public void setValue(final int index, final int axisIndex, final double value) {
    setValue(index, axisIndex, value);
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
    final int axisCount) {
    if (size() > 1 && coordinatesList.size() > 1) {
      if (getAxisCount() >= axisCount && coordinatesList.getAxisCount() >= axisCount) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < axisCount; j++) {
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
    final int axisCount = getAxisCount();
    final CoordinatesList target = create(length, axisCount);
    copy(sourceIndex, target, targetIndex, axisCount, count);
    return target;
  }

  @Override
  public Coordinates[] toCoordinateArray() {
    final Coordinates[] coordinateArray = new Coordinates[size()];
    for (int i = 0; i < coordinateArray.length; i++) {
      coordinateArray[i] = getCoordinateCopy(i);
    }
    return coordinateArray;
  }

  @Override
  public String toString() {
    final int axisCount = getAxisCount();
    if (axisCount > 0 && size() > 0) {
      final StringBuffer s = new StringBuffer("LINESTRING(");
      append(s, 0, axisCount);
      for (int i = 1; i < size(); i++) {
        s.append(',');
        append(s, i, axisCount);
      }
      s.append(')');
      return s.toString();
    } else {
      return "LINESTRING EMPTY";
    }
  }
}
