package com.revolsys.gis.jts;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateSequenceIndexLineSegment extends AbstractLineSegment {

  private final CoordinateSequence coordinates;

  private int index = -1;

  public CoordinateSequenceIndexLineSegment(
    final CoordinateSequence coordinates) {
    this.coordinates = coordinates;
  }

  public int getDimension() {
    return coordinates.getDimension();
  }

  public double getEndOrdinate(
    final int ordinateIndex) {
    return coordinates.getOrdinate(index + 1, ordinateIndex);
  }

  public int getIndex() {
    return index;
  }

  public double getStartOrdinate(
    final int ordinateIndex) {
    return coordinates.getOrdinate(index, ordinateIndex);
  }

  public void setIndex(
    final int index) {
    this.index = index;
  }

  public int size() {
    return coordinates.size() - 1;
  }
}
