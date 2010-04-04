package com.revolsys.gis.jts;

import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractLineSegment implements LineSegment {

  public AbstractLineSegment() {
    super();
  }

  public boolean contains(
    final Coordinate coordinate) {
    final double x = coordinate.x;
    final double y = coordinate.y;
    return (x == getStartX() && y == getStartY())
      || (x == getEndX() && y == getEndY());
  }

  public double getDistance(
    final Coordinate coordinate) {
    return getDistance(coordinate.x, coordinate.y);
  }

  public double getDistance(
    final double x,
    final double y) {
    final double x1 = getStartX();
    final double y1 = getStartY();
    final double x2 = getEndX();
    final double y2 = getEndY();
    return MathUtil.pointLineDistance(x, y, x1, y1, x2, y2);
  }

  public double[] getEndCoordinates() {
    final int dimension = getDimension();
    final double[] endCoordinates = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      endCoordinates[i] = getEndOrdinate(i);
    }
    return endCoordinates;
  }

  public double getEndDistance(
    final Coordinate coordinate) {
    return getEndDistance(coordinate.x, coordinate.y);
  }

  public double getEndDistance(
    final double x,
    final double y) {
    return MathUtil.distance(x, y, getStartX(), getStartY());
  }

  public double getEndX() {
    return getEndOrdinate(0);
  }

  public double getEndY() {
    return getEndOrdinate(1);
  }

  public Envelope getEnvelope() {
    final double startX = getStartX();
    final double endX = getEndX();
    final double startY = getStartY();
    final double endY = getEndY();
    return new Envelope(startX, endX, startY, endY);
  }

  public double[] getStartCoordinates() {
    final int dimension = getDimension();
    final double[] startCoordinates = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      startCoordinates[i] = getStartOrdinate(i);
    }
    return startCoordinates;
  }

  public double getStartDistance(
    final Coordinate coordinate) {
    return getStartDistance(coordinate.x, coordinate.y);
  }

  public double getStartDistance(
    final double x,
    final double y) {
    return MathUtil.distance(x, y, getEndX(), getEndY());
  }

  public double getStartX() {
    return getStartOrdinate(0);
  }

  public double getStartY() {
    return getStartOrdinate(1);
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer("LINESTRING(");
    string.append(getStartOrdinate(0));
    for (int i = 1; i < getDimension(); i++) {
      string.append(' ');
      string.append(getStartOrdinate(i));
    }
    string.append(',');
    string.append(getEndOrdinate(0));
    for (int i = 1; i < getDimension(); i++) {
      string.append(' ');
      string.append(getEndOrdinate(i));
    }
    string.append(')');
    return string.toString();
  }
}
