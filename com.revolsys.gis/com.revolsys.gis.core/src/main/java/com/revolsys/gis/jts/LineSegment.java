package com.revolsys.gis.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface LineSegment {
  public boolean contains(
    Coordinate coordinate);

  public int getDimension();

  public double getDistance(
    Coordinate coordinate);

  public double getDistance(
    double x,
    double y);

  public double[] getEndCoordinates();

  public double getEndDistance(
    Coordinate coordinate);

  public double getEndDistance(
    double x,
    double y);

  public double getEndOrdinate(
    int ordinateIndex);

  public double getEndX();

  public double getEndY();

  public Envelope getEnvelope();

  public double[] getStartCoordinates();

  public double getStartDistance(
    Coordinate coordinate);

  public double getStartDistance(
    double x,
    double y);

  public double getStartOrdinate(
    int ordinateIndex);

  public double getStartX();

  public double getStartY();
}
