package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.MathUtil;

public abstract class AbstractCoordinatesList implements PointList {

  /**
   * 
   */
  private static final long serialVersionUID = 9211011581013036939L;

  public void append(final StringBuffer s, final int i, final int axisCount) {
    s.append(getX(i));
    s.append(' ');
    s.append(getY(i));
    for (int j = 2; j < axisCount; j++) {
      final Double coordinate = getCoordinate(i, j);
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

  public PointList create(final int length, final int axisCount) {
    return new DoubleCoordinatesList(length, axisCount);
  }

  @Override
  public double distance(final int index, final Point point) {
    if (index < getVertexCount()) {
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
  public boolean equals(final Object object) {
    if (object instanceof PointList) {
      final PointList points = (PointList)object;
      return equals(points);
    } else {
      return false;
    }
  }

  public boolean equals2dCoordinate(final int index, final double x,
    final double y) {
    return getX(index) == x && getY(index) == y;
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int index,
    final Point point) {
    int maxAxis = Math.max(getAxisCount(), point.getAxisCount());
    if (maxAxis > axisCount) {
      maxAxis = axisCount;
    }
    if (getAxisCount() < maxAxis) {
      return false;
    } else if (point.getAxisCount() < maxAxis) {
      return false;
    } else if (index < getVertexCount()) {
      for (int j = 0; j < maxAxis; j++) {
        final double value1 = getCoordinate(index, j);
        final double value2 = point.getCoordinate(j);
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
  public double[] getCoordinates() {
    final int size = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[size * axisCount];
    int k = 0;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < axisCount; j++) {
        final double coordinate = getCoordinate(i, j);
        coordinates[k] = coordinate;
        k++;
      }
    }
    return coordinates;
  }

  @Override
  public double getM(final int index) {
    return getCoordinate(index, 3);
  }

  @Override
  public Point getPoint(final int vertexIndex) {
    if (vertexIndex >= 0 && vertexIndex < getVertexCount()) {
      final double[] values = new double[getAxisCount()];
      for (int axisIndex = 0; axisIndex < values.length; axisIndex++) {
        values[axisIndex] = getCoordinate(vertexIndex, axisIndex);
      }
      return new PointDouble(values);
    } else {
      return null;
    }
  }

  @Override
  public double getX(final int index) {
    return getCoordinate(index, 0);
  }

  @Override
  public double getY(final int index) {
    return getCoordinate(index, 1);
  }

  @Override
  public double getZ(final int index) {
    return getCoordinate(index, 2);
  }

  // @Override
  // public double getValue(final int index, final int axisIndex) {
  // return getValue(index, axisIndex);
  // }

  @Override
  public int hashCode() {
    int h = 0;
    for (int i = 0; i < getVertexCount(); i++) {
      for (int j = 0; j < getAxisCount(); j++) {
        h = 31 * h + ((Double)getCoordinate(i, j)).hashCode();
      }
    }
    return h;
  }

  @Override
  public boolean hasVertex(final Point point) {
    for (int i = 0; i < getVertexCount(); i++) {
      if (equalsVertex(2, i, point)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isCounterClockwise() {
    // # of points without closing endpoint
    final int nPts = getVertexCount() - 1;

    // find highest point
    double hiPtX = getX(0);
    double hiPtY = getY(0);
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final double x = getX(i);
      final double y = getY(i);
      if (y > hiPtY) {
        hiPtX = x;
        hiPtY = y;
        hiIndex = i;
      }
    }

    // find distinct point before highest point
    int iPrev = hiIndex;
    do {
      iPrev = iPrev - 1;
      if (iPrev < 0) {
        iPrev = nPts;
      }
    } while (equals2dCoordinate(iPrev, hiPtX, hiPtY) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % nPts;
    } while (equals2dCoordinate(iNext, hiPtX, hiPtY) && iNext != hiIndex);

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (equals2dCoordinate(iPrev, hiPtX, hiPtY)
      || equals2dCoordinate(iNext, hiPtX, hiPtY)
      || CoordinatesListUtil.equals2dCoordinates(this, iPrev, iNext)) {
      return false;
    }

    final int disc = CoordinatesListUtil.orientationIndex(this, iPrev, hiIndex,
      iNext);

    /**
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the lines
     * lie on top of one another (1) is handled by checking if next is left of
     * prev ==> CCW (2) will never happen if the ring is valid, so don't check
     * for it (Might want to assert this)
     */
    boolean counterClockwise = false;
    if (disc == 0) {
      // poly is CCW if prev x is right of next x
      final double prevX = getCoordinate(iPrev, 0);
      final double nextX = getCoordinate(iNext, 0);
      counterClockwise = (prevX > nextX);
    } else {
      // if area is positive, points are ordered CCW
      counterClockwise = (disc > 0);
    }
    return counterClockwise;
  }

  @Override
  public PointList reverse() {
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final int coordinateIndex = (vertexCount - 1 - vertexIndex) * axisCount
          + axisIndex;
        coordinates[coordinateIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  @Override
  public PointList subLine(final int index) {
    return subLine(index, getVertexCount() - index);
  }

  @Override
  public PointList subLine(final int index, final int count) {
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[count * axisCount];
    for (int i = 0; i < count; i++) {
      final Point point = getPoint(index + i);
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0 + i, point);
    }
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  @Override
  public List<Point> toPointList() {
    final List<Point> points = new ArrayList<>();
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getPoint(i);
      points.add(point.cloneCoordinates());
    }
    return points;
  }

  @Override
  public String toString() {
    final int axisCount = getAxisCount();
    if (axisCount > 0 && getVertexCount() > 0) {
      final StringBuffer s = new StringBuffer("LINESTRING(");
      append(s, 0, axisCount);
      for (int i = 1; i < getVertexCount(); i++) {
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
